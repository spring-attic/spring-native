package com.example.webclient;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class CLR implements CommandLineRunner {

	private WebClient client;

	public CLR(WebClient.Builder builder) {
		this.client = builder.baseUrl("https://protocoderspoint.com").build();
	}

	@Override
	public void run(String... args) throws Exception {
		this.client.get().uri("/jsondata/superheros.json").retrieve().bodyToMono(Data.class).subscribe(System.out::println);
	}
	
}