package com.example.commandlinerunner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CommandlinerunnerApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(CommandlinerunnerApplication.class, args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}

	@Bean
	public CommandLineRunner runner() {
		return args -> System.err.println("Hello World");
	}

}
