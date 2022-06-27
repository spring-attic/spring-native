package com.example.lombok;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class Slf4jConfiguration {

    @Bean
    ApplicationRunner slf4jRunner() {
        return event -> log.info("Hello, Slf4j world!");
    }

}
