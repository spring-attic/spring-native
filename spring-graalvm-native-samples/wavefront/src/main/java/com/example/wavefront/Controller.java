package com.example.wavefront;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
    
    @GetMapping("/")
    public String bar() {
        return "Hello from tomcat";
    }
    
}