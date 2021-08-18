package com.example.webflux;

import org.springframework.aot.SpringAotApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication	
public class SecurityWebfluxApplication {

	public static void main(String[] args) {
		SpringAotApplication.run(SecurityWebfluxApplication.class, args);
	}

}
