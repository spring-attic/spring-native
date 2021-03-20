package com.example.logj42;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

@Log4j2
@SpringBootApplication
public class Logj42Application {

	public static void main(String[] args) {
		SpringApplication.run(Logj42Application.class, args);
	}

	@Bean
	ApplicationRunner runner() {
		return event -> log.info("hello, world!");
	}
}
