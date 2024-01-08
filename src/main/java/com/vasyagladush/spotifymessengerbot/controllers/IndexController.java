package com.vasyagladush.spotifymessengerbot.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@RestController
@RequestMapping("/")
public class IndexController {
    @GetMapping
    @ResponseBody
    public String getIndex() {
        return "{\"cool\": true}";
    }
}