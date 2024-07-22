package com.matt.test.workload_identity.controllers;

import com.matt.test.workload_identity.repos.WiDemoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.matt.test.workload_identity.models.WiDemo;

@RestController
public class WIDemoController {

//    @Autowired
//    private ASBHandler asbHandler;

    @Autowired
    private WiDemoRepo repo;

//    @GetMapping("/sendAsb/{msg}")
//    public String sendAsb(@PathVariable("msg") String msg) {
//        return asbHandler.sendASBMsg(msg);
//    }

    @GetMapping("/saveToDB/{msg}")
    public String saveToDB(@PathVariable("msg") String msg) {
        try {
            repo.save(new WiDemo(0, msg));
        } catch (Exception e) {
            return String.format("Error in WIDemoController::saveToDB()::%s", e.getMessage());
        }
        return "Success save to DB";
    }

    @GetMapping("/hello")
    public String hello(@RequestParam String keyWord) {
        return "hello " + keyWord;
    }
}