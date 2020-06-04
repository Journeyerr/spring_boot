package com.zayan.www.rabbitmq.publisher;

import com.alibaba.fastjson.JSONObject;
import com.zayan.www.constant.enums.RabbitMqMessageConstant;
import com.zayan.www.model.entity.User;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author AnYuan
 * @date 03-12
 */

@Component
public class UserPublish {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send(User user) {
        rabbitTemplate.convertAndSend(RabbitMqMessageConstant.USER_EXCHANGE, "", JSONObject.toJSONString(user));
    }
}