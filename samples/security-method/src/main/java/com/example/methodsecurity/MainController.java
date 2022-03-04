package com.example.methodsecurity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    final GreetingService greetingService;

    public MainController(GreetingService greetingService) {
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

    @GetMapping("/filter/hello")
    public String helloFilter() {
        List<String> greetings = new ArrayList<>();
        greetings.add("Hello");
        greetings.add("Goodbye");
        return greetingService.echo(greetings);
    }

}
