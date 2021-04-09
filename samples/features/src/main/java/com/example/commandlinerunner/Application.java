package com.example.commandlinerunner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import elsewhere.FooBeanFactory;

@SpringBootApplication
@Import({FooBeanFactory.class,ConditionalConfig.class})
public class Application {
	
	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(Application.class, args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}
	
}
