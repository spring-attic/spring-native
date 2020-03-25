package com.example.demo;

import java.util.function.Function;

public class Foobar implements Function<String, String> {

    @Override
    public String apply(String input) {
        System.err.println("HI: " + input);
        return "hi " + input + "!";
    }
}