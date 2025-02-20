package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisTestController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/redis-test")
    public String testRedisConnection() {
        try {
            redisTemplate.opsForValue().set("testKey", "testValue");
            String value = redisTemplate.opsForValue().get("testKey");
            return "Redis connection is successful. Value: " + value;
        } catch (Exception e) {
            return "Redis connection failed: " + e.getMessage();
        }
    }
}
