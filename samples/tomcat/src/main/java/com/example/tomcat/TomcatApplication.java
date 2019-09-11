package com.example.tomcat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(proxyBeanMethods = false)
public class TomcatApplication {

	public static void main(String[] args) {
		SpringApplication.run(TomcatApplication.class, args);
	}

}
