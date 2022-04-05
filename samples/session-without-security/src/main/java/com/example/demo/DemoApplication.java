package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;

@SpringBootApplication
// For Thymeleaf
@NativeHint(types = @TypeHint(typeNames = "org.springframework.web.server.session.InMemoryWebSessionStore$InMemoryWebSession", access = TypeAccess.PUBLIC_METHODS))
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
