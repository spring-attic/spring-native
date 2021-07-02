package com.example.logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LoggerApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(LoggerApplication.class, args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}

	@Bean
	public CommandLineRunner runner() {
		return args -> {
			Log LOGGER = LogFactory.getLog(LoggerApplication.class);
			LOGGER.info("info");
			LOGGER.error("ouch", new RuntimeException());
		};
	}

}
