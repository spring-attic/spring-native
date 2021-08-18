package com.example.session;

import org.springframework.aot.SpringAotApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SessionJdbcApplication {

	public static void main(String[] args) throws Throwable {
		SpringAotApplication.run(SessionJdbcApplication.class, args);
	}

}
