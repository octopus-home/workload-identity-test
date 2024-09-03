package com.matt.test.workload_identity.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class WebController {
    @Autowired
    private StringRedisTemplate template;

    @GetMapping("/hello")
    public String hello(String msg) {
        return "hello " + msg;
    }

    @PostMapping("/add")
    public String add(String key, String value) {
        log.info("key:{}, value:{}", key, value);
        ValueOperations<String, String> operations = template.opsForValue();
        operations.set(key, value);
        return "success";
    }

    @GetMapping("/get")
    public String get(String key) {
        log.info("key {}", key);
        ValueOperations<String, String> operations = template.opsForValue();
        String value = operations.get(key);
        return value;
    }
}
