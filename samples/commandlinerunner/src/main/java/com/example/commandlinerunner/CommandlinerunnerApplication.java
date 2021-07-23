package com.example.commandlinerunner;

import org.springframework.aot.SpringAotApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CommandlinerunnerApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringAotApplication.run(CommandlinerunnerApplication.class, args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}
	
}
