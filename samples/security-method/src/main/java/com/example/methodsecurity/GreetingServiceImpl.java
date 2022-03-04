package com.example.methodsecurity;

import java.util.List;

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

    @Override
    public String echo(List<String> greetings) {
        return String.join(",", greetings);
    }
}
