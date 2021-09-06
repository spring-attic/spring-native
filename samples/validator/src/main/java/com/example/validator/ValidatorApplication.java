package com.example.validator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(Foo.class)
public class ValidatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(ValidatorApplication.class, args);
	}

}

