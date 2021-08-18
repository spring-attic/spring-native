package com.example.rsocket;

import org.springframework.aot.SpringAotApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RSocketApplication {

	public static void main(String[] args) {
		SpringAotApplication.run(RSocketApplication.class, args);
	}

}
