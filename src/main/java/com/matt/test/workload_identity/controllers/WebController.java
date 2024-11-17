package com.matt.test.workload_identity.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
public class WebController {
    @Autowired
    private StringRedisTemplate template;
    @Autowired
    private RedissonClient redissonClient;

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

    @GetMapping("/env")
    public String env() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
//        System.out.println(mapper.writeValueAsString(environment));
        System.out.println("===================================");
        Map<String, String> envMap = System.getenv();
        for (Map.Entry<String, String> entry : envMap.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
        return mapper.writeValueAsString(envMap);
    }


    @PostMapping("/redisson")
    public String redisson(String key, String value) {
        log.info("key:{}, value:{}", key, value);

        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(value);

        System.out.println("Value of mykey: " + bucket.get());

        return "success";
    }

    @GetMapping("/redissonGet")
    public String redissonGet(String key) {
        log.info("key:{}", key);

        RBucket<String> bucket = redissonClient.getBucket(key);

        System.out.println("Value of " + key + ": " + bucket.get());

        return bucket.get();
    }


}
