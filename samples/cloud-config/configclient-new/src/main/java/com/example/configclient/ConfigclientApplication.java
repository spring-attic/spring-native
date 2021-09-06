package com.example.configclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ConfigclientApplication {


	public static void main(String[] args) {
		SpringApplication.run(ConfigclientApplication.class, args);
		try { Thread.sleep(20000); } catch (Exception e) {}
	}

	@Bean
	CommandLineRunner commandLineRunner(@Value("${my.prop}") String injected) {
		return args -> System.out.println(":" + injected);
	}
}
