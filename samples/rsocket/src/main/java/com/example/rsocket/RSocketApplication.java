package com.example.rsocket;

import io.rsocket.internal.UnboundedProcessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication
@ImportRuntimeHints(NettyRuntimeHints.class)
public class RSocketApplication {

	public static void main(String[] args) {
		// TODO MH: Remove
		new UnboundedProcessor();
		SpringApplication.run(RSocketApplication.class, args);
	}

}
