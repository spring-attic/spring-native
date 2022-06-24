package com.example.events;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication
@ImportRuntimeHints(HikariRuntimeHintsRegistrar.class)
public class EventApplication {
	
	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(EventApplication.class, args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}
	
}
