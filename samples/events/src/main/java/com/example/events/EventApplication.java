package com.example.events;

import org.springframework.aot.thirdpartyhints.HikariRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication
@ImportRuntimeHints(HikariRuntimeHints.class)
public class EventApplication {
	
	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(EventApplication.class, args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}
	
}
