package com.zayan.www.config.redis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author AnYuan
 */
@Data
@Component
@ConfigurationProperties( prefix = "spring.redis-two")
public class RedisTwoProperties {

    private String host;
    private int port;
    private int timeout;
    private int database;
    private int maxWait;
    private int maxActive;
    private int maxIdle;
    private int minIdle;
}
