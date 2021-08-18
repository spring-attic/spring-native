package com.example.async;

import org.springframework.aot.SpringAotApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AsyncApplication {
	
	public static void main(String[] args) throws InterruptedException {
		SpringAotApplication.run(AsyncApplication.class, args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}
	
}
