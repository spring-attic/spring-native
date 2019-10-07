package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Foobar {

    @GetMapping("/x")
    public String greet2() {
        return "hix!";
    }
}