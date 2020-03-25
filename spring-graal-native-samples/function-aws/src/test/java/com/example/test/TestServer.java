package com.example.test;

import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

@SpringBootApplication(proxyBeanMethods = false)
public class TestServer {

    private MonoProcessor<String> output = MonoProcessor.<String>create();

    private String response = "";

    public static void main(String[] args) {
        SpringApplication.run(TestServer.class, "--server.port=8000", "--spring.cloud.function.web.export.enabled=false");
    }

    @Bean
    public Supplier<Mono<String>> home() {
        return () -> output;
    }

    @Bean
    public Function<String, String> echo() {
        return input -> {
            response = input;
            return "Echo: " + input;
        };
    }

    @Bean
    public Function<String, String> add() {
        return input -> {
            System.err.println("Add: " + input);
            output.onNext(input);
            // output.onComplete(); // Just one event
            return "Added: " + input;
        };
    }

    @Bean
    public Supplier<String> take() {
        return () -> response;
    }

}