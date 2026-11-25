package com.auca.lostandfound.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
                "name", "Campus Lost and Found",
                "status", "running",
                "api", "/api",
                "items", "/api/items",
                "h2Console", "/h2-console"
        );
    }
}
