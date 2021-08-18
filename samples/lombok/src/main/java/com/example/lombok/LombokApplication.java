package com.example.lombok;

import org.springframework.aot.SpringAotApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LombokApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringAotApplication.run(LombokApplication.class, args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}
}
