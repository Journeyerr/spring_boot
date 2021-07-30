package com.zayan.www.rabbitmq.consumers;

import com.alibaba.fastjson.JSONObject;
import com.mysql.jdbc.TimeUtil;
import com.zayan.www.constant.RabbitMqConstant;
import com.zayan.www.constant.RedisConstant;
import com.zayan.www.constant.enums.SecKillTraceIdStatusEnum;
import com.zayan.www.model.entity.SeckillOrder;
import com.zayan.www.model.entity.User;
import com.zayan.www.model.form.test.SecKillOrderCreateForm;
import com.zayan.www.model.vo.BaseResult;
import com.zayan.www.service.SeckillOrderService;
import com.zayan.www.util.DateUtil;
import com.zayan.www.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * rabbit 消费秒杀队列
 *
 * @author AnYuan
 * @date 2021-01-07
 */

@Component
@Slf4j
public class SecKillOrderConsumer {

    @Autowired
    @Qualifier("stringRedisTemplate")
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private SeckillOrderService seckillOrderService;

    @RabbitListener(concurrency = "3",
            bindings = @QueueBinding(
            value = @Queue(value = RabbitMqConstant.SECKILL_ORDER_TRACE_QUEUE, durable = "true"),
            exchange = @Exchange(value = RabbitMqConstant.SECKILL_ORDER_TRACE_EXCHANGE)
    ))
    public void onMassageUser(Message message) {
        log.info("SecKillOrderConsumer 开始消费----->{}", message);

        // 消费者判断状态->>消费mq， 执行下单前判断库存是否充足，充足再进行下单操作，成功后更新trace 如果消费失败则补充库存 （order 入库）
        SecKillOrderCreateForm createForm = JSONObject.parseObject(new String(message.getBody()), SecKillOrderCreateForm.class);

        String stockKey = RedisConstant.secKillSkuStockKey(createForm.getSkuNo().toString());
        String traceIdKey = RedisConstant.secKillTraceIdKey(createForm.getTraceId());

        Long decrementStock = stringRedisTemplate.opsForValue().decrement(stockKey);
        if (Objects.isNull(decrementStock) || decrementStock.compareTo(0L) < 0) {
            stringRedisTemplate.opsForValue().set(traceIdKey, SecKillTraceIdStatusEnum.FAIL.getCode());
            return;
        }

        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(createForm.getUserId());
        seckillOrder.setSkuNo(createForm.getSkuNo());
        seckillOrder.setNo(StringUtil.no());
        seckillOrder.setTraceId(createForm.getTraceId());

        boolean save = seckillOrderService.save(seckillOrder);
        if (save) {
            stringRedisTemplate.opsForValue().set(traceIdKey, SecKillTraceIdStatusEnum.SUBMIT.getCode());
        }else {
            stringRedisTemplate.opsForValue().set(traceIdKey, SecKillTraceIdStatusEnum.FAIL.getCode());
            stringRedisTemplate.opsForValue().increment(stockKey);
        }
        stringRedisTemplate.expire(traceIdKey, 1, TimeUnit.DAYS);

        log.info("SecKillOrderConsumer 消费完成----->{}", createForm.getTraceId());
    }
}
