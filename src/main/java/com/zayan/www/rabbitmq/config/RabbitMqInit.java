package com.zayan.www.rabbitmq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * @author AnYuan
 * @date 03-12
 */

//@Configuration
@Slf4j
public class RabbitMqInit {

    //    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        log.info("rabbitTemplate init");
        return rabbitTemplate;
    }

    //    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        log.info("rabbitAdmin init");
        return rabbitAdmin;
    }
}
