package com.example.webclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class CLR implements CommandLineRunner {

	Logger logger = LoggerFactory.getLogger(CLR.class);

	private WebClient client;

	public CLR(WebClient.Builder builder) {
		this.client = builder.baseUrl("http://localhost:8080").build();
	}

	@Override
	public void run(String... args) {
		this.client.get().uri("superheros.json").retrieve().bodyToMono(Data.class).subscribe(message -> logger.info(message.toString()));
	}
	
}