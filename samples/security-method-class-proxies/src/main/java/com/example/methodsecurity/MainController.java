package com.example.methodsecurity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    final GreetingServiceImpl greetingService;

    public MainController(GreetingServiceImpl greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping("/hello")
    public String hello() {
        return greetingService.hello();
    }

    @GetMapping("/admin/hello")
    public String adminHello() {
        return greetingService.adminHello();
    }

}
