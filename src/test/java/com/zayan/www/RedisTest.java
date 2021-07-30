package com.zayan.www;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedisTest {

    @Autowired
    @Qualifier("stringRedisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    @Qualifier("stringRedisTwoTemplate")
    private RedisTemplate<String, String> redisTwoTemplate;

    @Test
    public void redisTest() {
        System.out.println("start----------");
        redisTemplate.opsForValue().set("redis", "success", 1, TimeUnit.DAYS);
        redisTwoTemplate.opsForValue().set("twoRedis", "success", 1, TimeUnit.DAYS);
        System.out.println("end----------");

    }

}
