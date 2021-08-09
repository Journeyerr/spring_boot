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
    private RedisTwoProperties redisTwoProperties;

    @Bean
    public <T> RedisTemplate<String, T> redisTemplateLocal() {

        // 基本配置
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(redisTwoProperties.getHost());
        configuration.setPort(redisTwoProperties.getPort());
        configuration.setDatabase(redisTwoProperties.getDatabase());
        if (ObjectUtils.isNotEmpty(redisTwoProperties.getPassword())) {
            configuration.setPassword(RedisPassword.of(redisTwoProperties.getPassword()));
        }

        // 连接池配置
        GenericObjectPoolConfig<Object> genericObjectPoolConfig = new GenericObjectPoolConfig<>();
        genericObjectPoolConfig.setMaxTotal(redisTwoProperties.getMaxActive());
        genericObjectPoolConfig.setMaxWaitMillis(redisTwoProperties.getMaxWait());
        genericObjectPoolConfig.setMaxIdle(redisTwoProperties.getMaxIdle());
        genericObjectPoolConfig.setMinIdle(redisTwoProperties.getMinIdle());

        // lettuce pool
        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder = LettucePoolingClientConfiguration.builder();
        builder.poolConfig(genericObjectPoolConfig);
        builder.commandTimeout(Duration.ofSeconds(redisTwoProperties.getTimeout()));
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
