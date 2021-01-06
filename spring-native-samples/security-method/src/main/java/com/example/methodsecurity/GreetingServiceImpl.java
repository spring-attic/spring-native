package com.example.methodsecurity;

import org.springframework.stereotype.Service;

@Service
public class GreetingServiceImpl implements GreetingService {

    @Override
    public String hello() {
        return "Hello!";
    }

    @Override
    public String adminHello() {
        return "Goodbye!";
    }
}
