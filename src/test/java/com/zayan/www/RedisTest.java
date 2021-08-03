package com.zayan.www;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedisTest {

    @Autowired
    @Qualifier("redisTemplateLocal")
    private RedisTemplate<String, String> redisTemplateLocal;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;



    @Test
    public void redisTest() {
        System.out.println("start----------");
        redisTemplateLocal.opsForValue().set("redisTemplateLocal", "success", 1, TimeUnit.DAYS);
        redisTemplate.opsForValue().set("redisTemplate", "success", 1, TimeUnit.DAYS);
        stringRedisTemplate.opsForValue().set("stringRedisTemplate", "success", 1, TimeUnit.DAYS);
        System.out.println("end----------");

    }
}
