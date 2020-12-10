package com.example.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication	
public class SecuringWebfluxApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecuringWebfluxApplication.class, args);
	}

}
