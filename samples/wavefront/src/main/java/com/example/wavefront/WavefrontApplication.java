package com.example.wavefront;

import org.springframework.aot.SpringAotApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WavefrontApplication {

	public static void main(String[] args) {
		SpringAotApplication.run(WavefrontApplication.class, args);
	}

}
