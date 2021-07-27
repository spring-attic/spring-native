package com.example.webflux;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import reactor.core.publisher.Mono;

@Controller
public class MainController {
    @GetMapping("/")
    public String index() {
        return "home";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/admin")
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<String> admin() {
		return Mono.just("admin");
    }
}