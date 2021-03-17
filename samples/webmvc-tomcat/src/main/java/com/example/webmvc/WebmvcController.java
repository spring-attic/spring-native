package com.example.webmvc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebmvcController {
    
    @GetMapping("/")
    public String foo() {
        return "Hello from Spring MVC and Tomcat";
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @GetMapping("/status")
    public String status() {
        return "status";
    }
    
}