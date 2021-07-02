package com.example.demo;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

@Endpoint(id="custom")
@Component
public class Custom {
    
    @ReadOperation
    public String health() {
        return "OK";
    }
    
    
}
