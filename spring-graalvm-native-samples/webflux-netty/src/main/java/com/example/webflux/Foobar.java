package com.example.webflux;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
public class Foobar {

    @GetMapping("/x")
    public String greet2() {
        return "hix!";
    }
    
    @GetMapping("/hello")
    public Mono<String> hello() {
      return Mono.just("World");
    }
}