package com.example.demo;

import java.util.function.Function;

import org.springframework.stereotype.Component;

@Component
public class Foobar implements Function<String, String> {

    @Override
    public String apply(String input) {
        return "hi " + input + "!";
    }
}