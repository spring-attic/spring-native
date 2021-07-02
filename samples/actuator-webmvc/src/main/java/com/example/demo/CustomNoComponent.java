package com.example.demo;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

@Endpoint(id="customnc")
public class CustomNoComponent {
    
    @ReadOperation
    public String health() {
        return "OK";
    }
    
    
}
