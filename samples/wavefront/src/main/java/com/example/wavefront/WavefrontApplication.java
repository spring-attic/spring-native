package com.example.wavefront;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.wavefront.WavefrontAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = WavefrontAutoConfiguration.class)
public class WavefrontApplication {

	public static void main(String[] args) {
		SpringApplication.run(WavefrontApplication.class, args);
	}

}
