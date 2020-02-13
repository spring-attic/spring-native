package com.example.undertow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(proxyBeanMethods = false)
public class UndertowApplication {

	public static void main(String[] args) {
		SpringApplication.run(UndertowApplication.class, args);
	}

}
