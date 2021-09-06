package com.example.lombok;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LombokApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(LombokApplication.class, args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}
}
