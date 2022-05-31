package com.example.events;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EventApplication {
	
	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(EventApplication.class, args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}
	
}
