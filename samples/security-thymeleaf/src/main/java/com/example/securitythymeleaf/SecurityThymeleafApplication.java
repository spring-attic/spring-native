package com.example.securitythymeleaf;

import org.springframework.aot.thirdpartyhints.ThymeleafRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication
@ImportRuntimeHints(ThymeleafRuntimeHints.class)
public class SecurityThymeleafApplication {

	public static void main(String[] args) throws Throwable {
		SpringApplication.run(SecurityThymeleafApplication.class, args);
	}

}
