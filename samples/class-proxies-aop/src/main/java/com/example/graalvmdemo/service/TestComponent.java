package com.example.graalvmdemo.service;

import org.springframework.stereotype.Component;

/**
 * @author Moritz Halbritter
 */
@Component
public class TestComponent {
    public String methodA() {
        return "A";
    }

    public String methodB() {
        return "B";
    }
}
