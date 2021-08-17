package com.zayan.www.config.redis;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfigure extends CachingConfigurerSupport {

    @Autowired
    private UserRedisProperties userRedisProperties;

    @Bean
    public <T> RedisTemplate<String, T> redisTemplateLocal() {

        // 基本配置
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(userRedisProperties.getHost());
        configuration.setPort(userRedisProperties.getPort());
        configuration.setDatabase(userRedisProperties.getDatabase());
        if (ObjectUtils.isNotEmpty(userRedisProperties.getPassword())) {
            configuration.setPassword(RedisPassword.of(userRedisProperties.getPassword()));
        }

        // 连接池配置
        GenericObjectPoolConfig<Object> genericObjectPoolConfig = new GenericObjectPoolConfig<>();
        genericObjectPoolConfig.setMaxTotal(userRedisProperties.getMaxActive());
        genericObjectPoolConfig.setMaxWaitMillis(userRedisProperties.getMaxWait());
        genericObjectPoolConfig.setMaxIdle(userRedisProperties.getMaxIdle());
        genericObjectPoolConfig.setMinIdle(userRedisProperties.getMinIdle());

        // lettuce pool
        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder = LettucePoolingClientConfiguration.builder();
        builder.poolConfig(genericObjectPoolConfig);
        builder.commandTimeout(Duration.ofSeconds(userRedisProperties.getTimeout()));
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(configuration, builder.build());

        lettuceConnectionFactory.afterPropertiesSet();
        return createRedisTemplate(lettuceConnectionFactory);
    }

    private <T> RedisTemplate<String, T> createRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, T> stringObjectRedisTemplate = new RedisTemplate<>();
        stringObjectRedisTemplate.setConnectionFactory(redisConnectionFactory);

        RedisSerializer<String> redisSerializer = new StringRedisSerializer();

        //key序列化方式
        stringObjectRedisTemplate.setKeySerializer(redisSerializer);
        //value序列化
        stringObjectRedisTemplate.setValueSerializer(redisSerializer);
        //value hashMap序列化
        stringObjectRedisTemplate.setHashValueSerializer(redisSerializer);
        //key haspMap序列化
        stringObjectRedisTemplate.setHashKeySerializer(redisSerializer);
        
        return stringObjectRedisTemplate;
    }
}
