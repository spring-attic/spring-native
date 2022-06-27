package com.example.graalvmdemo;

import com.example.graalvmdemo.service.TestComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GraalvmDemoApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraalvmDemoApplication.class);

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(GraalvmDemoApplication.class, args);
        Thread.currentThread().join(); // To be able to measure memory consumption
    }

    @Bean
    CommandLineRunner commandLineRunner(TestComponent testComponent) {
        return args -> {
            LOGGER.info("methodA: {}", testComponent.methodA());
            LOGGER.info("methodB: {}", testComponent.methodB());
        };
    }

}
