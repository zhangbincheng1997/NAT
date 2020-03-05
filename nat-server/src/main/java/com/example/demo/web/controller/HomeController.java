package com.example.demo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/password")
    public String password() {
        return "password";
    }

    @GetMapping("/blacklist")
    public String blacklist() {
        return "blacklist";
    }

    @GetMapping("/network")
    public String network() {
        return "network";
    }
}
