package com.example.zipkin.sleuth;

import org.springframework.aot.SpringAotApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.nativex.hint.NativeHint;

@SpringBootApplication
@NativeHint(options = "--enable-url-protocols=http")
public class ZipkinSleuthApplication {

	public static void main(String[] args) {
		SpringAotApplication.run(ZipkinSleuthApplication.class, args);
	}

}
