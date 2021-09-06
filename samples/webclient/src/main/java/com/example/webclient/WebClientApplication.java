package com.example.webclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.nativex.hint.TypeHint;

@TypeHint(types = Data.class, typeNames = "com.example.webclient.Data$SuperHero")
@SpringBootApplication
public class WebClientApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(WebClientApplication.class, args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}
	
}
