package com.example.methodsecurity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class GreetingServiceImpl {

    public String hello() {
        return "Hello!";
    }

    @PreAuthorize("hasRole('ADMIN')")
    public String adminHello() {
        return "Goodbye!";
    }
}
