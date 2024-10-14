package com.matt.test.workload_identity.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.matt.test.workload_identity.models.WiDemo;
import com.matt.test.workload_identity.repos.WiDemoRepo;
import com.matt.test.workload_identity.utils.ASBHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Sinks;

import java.util.Map;

@RestController
@Slf4j
public class WIDemoController {

    @Autowired
    private ASBHandler asbHandler;

//    @Autowired
//    private WiDemoRepo repo;

    @Autowired
    private Environment environment;

    @GetMapping("/azureASB/{msg}")
    public String sendAsb(@PathVariable("msg") String msg) {
        return asbHandler.sendASBMsg(msg);
    }

    @GetMapping("/saveToDB/{msg}")
    public String saveToDB(@PathVariable("msg") String msg) {
//        try {
//            repo.save(new WiDemo(0, msg));
//        } catch (Exception e) {
//            return String.format("Error in WIDemoController::saveToDB()::%s", e.getMessage());
//        }
        return "Success save to DB";
    }

    @GetMapping("/hello")
    public String hello(@RequestParam String keyWord) {
        return "hello " + keyWord;
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


    @Autowired
    private Sinks.Many<Message<String>> many;

    @GetMapping("/springCloudASB")
    public ResponseEntity<String> sendMessage(@RequestParam String message) {
        log.info("Going to add message {} to Sinks.Many.", message);
        many.emitNext(MessageBuilder.withPayload(message).build(), Sinks.EmitFailureHandler.FAIL_FAST);
        return ResponseEntity.ok("Sent!");
    }
}