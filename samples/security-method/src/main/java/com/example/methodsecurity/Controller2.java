package com.example.methodsecurity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller2 {

    @GetMapping("/admin/private")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminHello2() {
        return "bye";
    }

}
