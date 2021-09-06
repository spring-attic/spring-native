package com.example.zipkin.sleuth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.nativex.hint.NativeHint;

@SpringBootApplication
@NativeHint(options = "--enable-url-protocols=http")
public class ZipkinSleuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZipkinSleuthApplication.class, args);
	}

}
