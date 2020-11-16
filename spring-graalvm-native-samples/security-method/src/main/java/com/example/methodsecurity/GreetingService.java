package com.example.methodsecurity;

import org.springframework.security.access.prepost.PreAuthorize;

public interface GreetingService {

    String hello();

    @PreAuthorize("hasRole('ADMIN')")
    String adminHello();
}
