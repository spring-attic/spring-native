package com.example.methodsecurity;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;

public interface GreetingService {

    String hello();

    @PreAuthorize("hasRole('ADMIN')")
    String adminHello();

    @PreFilter("filterObject == 'Hello'")
    String echo(List<String> greetings);
}
