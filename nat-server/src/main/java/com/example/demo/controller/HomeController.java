package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/user")
    public String user() {
        return "user";
    }

    @GetMapping("/network")
    public String network() {
        return "network";
    }

    @GetMapping("/password")
    public String password() {
        return "password";
    }
}
