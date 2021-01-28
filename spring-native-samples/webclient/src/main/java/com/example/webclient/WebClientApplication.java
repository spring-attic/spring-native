package com.example.webclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.nativex.extension.NativeHint;
import org.springframework.nativex.extension.TypeInfo;

@NativeHint(typeInfos = @TypeInfo(types = Data.class, typeNames = "com.example.webclient.Data$SuperHero"))
@SpringBootApplication
public class WebClientApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(WebClientApplication.class, args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}
	
}
