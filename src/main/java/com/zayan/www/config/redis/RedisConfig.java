package com.zayan.www.config.redis;


import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.Resource;
import java.time.Duration;

/**
 * redis 多数据源配置
 * @author AnYuan
 * @date 2021-07-30
 */

@Configuration
public class RedisConfig {

    @Resource
    private RedisTwoProperties redisTwo;

    @Bean(name = {"redisTemplate"})
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<Object, Object>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean(name = {"stringRedisTemplate"})
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return createRedisTemplate(redisConnectionFactory);
    }

    @Bean("stringRedisTwoTemplate")
    public StringRedisTemplate stringRedisTwoTemplate() {

        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(redisTwo.getHost());
        configuration.setPort(redisTwo.getPort());
        configuration.setDatabase(redisTwo.getDatabase());

        GenericObjectPoolConfig<String> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxWaitMillis(redisTwo.getMaxWait());
        poolConfig.setMinIdle(redisTwo.getMinIdle());
        poolConfig.setMaxIdle(redisTwo.getMaxIdle());
        poolConfig.setMaxTotal(redisTwo.getMaxActive());

        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder = LettucePoolingClientConfiguration.builder();
        builder.poolConfig(poolConfig);
        builder.commandTimeout(Duration.ofSeconds(redisTwo.getTimeout()));

        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(configuration, builder.build());
        lettuceConnectionFactory.afterPropertiesSet();

        return createRedisTemplate(lettuceConnectionFactory);
    }

    private StringRedisTemplate createRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory);

        RedisSerializer<String> redisSerializer = new StringRedisSerializer();

        //key序列化方式
        stringRedisTemplate.setKeySerializer(redisSerializer);
        //value序列化
        stringRedisTemplate.setValueSerializer(redisSerializer);
        //value hashmap序列化
        stringRedisTemplate.setHashValueSerializer(redisSerializer);
        //key haspmap序列化
        stringRedisTemplate.setHashKeySerializer(redisSerializer);

        return stringRedisTemplate;
    }
}
