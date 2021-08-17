package com.zayan.www.config.redis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author AnYuan
 */
@Data
@Component
@ConfigurationProperties( prefix = "spring.redis-user")
public class UserRedisProperties {

    private String host;
    private String password;
    private int port;
    private int timeout;
    private int database;
    private int maxWait;
    private int maxActive;
    private int maxIdle;
    private int minIdle;
}
