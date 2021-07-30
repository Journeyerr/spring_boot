package com.zayan.www.controller.test;

import com.alibaba.fastjson.JSONObject;
import com.zayan.www.constant.RedisConstant;
import com.zayan.www.constant.enums.SecKillOrderStatusEnum;
import com.zayan.www.constant.enums.SecKillTraceIdStatusEnum;
import com.zayan.www.model.entity.SeckillOrder;
import com.zayan.www.model.entity.Skus;
import com.zayan.www.model.form.test.SecKillOrderCreateForm;
import com.zayan.www.model.form.test.SecKillOrderPaymentForm;
import com.zayan.www.model.vo.BaseResult;
import com.zayan.www.model.vo.test.SecKillOrderCreateVO;
import com.zayan.www.repository.SkusMapper;
import com.zayan.www.service.AopTestService;
import com.zayan.www.service.RabbitMqService;
import com.zayan.www.service.SeckillOrderService;
import com.zayan.www.service.SkusService;
import com.zayan.www.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author AnYuan
 */
@RestController
@Slf4j
@RequestMapping("/test/seckill/order")
public class SecKillOrderController {

    @Autowired
    private AopTestService aopTestService;
    @Autowired
    private SkusService skusService;
    @Qualifier("stringRedisTemplate")
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private SeckillOrderService seckillOrderService;
    @Autowired
    private SkusMapper skusMapper;
    @Autowired
    private RabbitMqService rabbitMqService;

    @GetMapping("/int/stock/{skuNo}")
    public BaseResult<?> intStock(@PathVariable Integer skuNo) {
        Skus skus = skusService.getByNo(skuNo);
        stringRedisTemplate.opsForValue().set(RedisConstant.secKillSkuStockKey(skuNo.toString()), skus.getStock().toString());
        return BaseResult.success();
    }

    @PostMapping("/create")
    public BaseResult<?> create(@Valid @RequestBody SecKillOrderCreateForm createForm) {

        // redis 校验库存
        String stock = stringRedisTemplate.opsForValue().get(RedisConstant.secKillSkuStockKey(createForm.getSkuNo().toString()));
        if (Objects.isNull(stock) || Integer.valueOf(stock).compareTo(0) < 1) {
            return BaseResult.error("Stock Is Null");
        }

        String traceId = StringUtil.getUuid(32);

        // trace=> status（成功 or 未下单 or 失败） 存入redis
        String secKillTraceIdKey = RedisConstant.secKillTraceIdKey(traceId);

        stringRedisTemplate.opsForValue().set(secKillTraceIdKey, SecKillTraceIdStatusEnum.NO_SUBMIT.getCode());
        createForm.setTraceId(traceId);

        // 放入mq
        rabbitMqService.sendSecKillOrderExchange(JSONObject.toJSONString(createForm));

        // 定时器扫描trace 是否成功支付，回收库存

        // 消费者判断状态->>消费mq， 执行下单前判断库存是否充足，充足再进行下单操作，成功后更新trace 如果消费失败则补充库存 （order 入库）

        // 整个过程返回trace给前端，前端轮循是否下单成功，成功则调起支付，失败则库存不足

        // 支付成功后扣减mysql库存

        return BaseResult.success(new SecKillOrderCreateVO(traceId, SecKillTraceIdStatusEnum.NO_SUBMIT));
    }

    @GetMapping("check/status/{traceId}")
    public BaseResult<?> checkStatus(@PathVariable String traceId) {

        String secKillTraceIdKey = RedisConstant.secKillTraceIdKey(traceId);
        String s = stringRedisTemplate.opsForValue().get(secKillTraceIdKey);
        if (Objects.isNull(s)) {
            return BaseResult.error("Forbiddent");
        }

        SeckillOrder seckillOrder = seckillOrderService.getByTraceId(traceId);
        if (Objects.nonNull(seckillOrder)) {
            return BaseResult.success(seckillOrder);
        }

        return BaseResult.error("TraceId: "+ s);
    }

    @PostMapping("payment")
    public BaseResult<?> payment(@Valid @RequestBody SecKillOrderPaymentForm paymentForm) {

        SeckillOrder seckillOrder = seckillOrderService.getByNoAndUserId(paymentForm.getNo(), paymentForm.getUserId());
        if (Objects.isNull(seckillOrder)) {
            return BaseResult.error("Forbiddent");
        }

        if (SecKillTraceIdStatusEnum.PAID.getCode().equals(seckillOrder.getStatus())) {
            return BaseResult.success(seckillOrder);
        }

        seckillOrder.setStatus(SecKillOrderStatusEnum.PAID.getCode());
        boolean update = seckillOrderService.updateById(seckillOrder);
        if (update) {
            String secKillTraceIdKey = RedisConstant.secKillTraceIdKey(paymentForm.getNo());
            stringRedisTemplate.opsForValue().set(secKillTraceIdKey, SecKillTraceIdStatusEnum.PAID.name());
            stringRedisTemplate.expire(secKillTraceIdKey, 1, TimeUnit.DAYS);

            skusMapper.decrementStock(seckillOrder.getSkuNo());
            return BaseResult.success(seckillOrder);
        }
        return BaseResult.error("NetWork Error");
    }
}
