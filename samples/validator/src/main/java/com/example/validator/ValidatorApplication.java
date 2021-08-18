package com.example.validator;

import org.springframework.aot.SpringAotApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(Foo.class)
public class ValidatorApplication {

	public static void main(String[] args) {
		SpringAotApplication.run(ValidatorApplication.class, args);
	}

}

