package com.example.webclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class HttpsWebClientBuilderRequester implements CommandLineRunner {

	Logger logger = LoggerFactory.getLogger(HttpsRequester.class);

	private WebClient client;

	public HttpsWebClientBuilderRequester() {
		this.client = WebClient.builder().baseUrl("https://httpbin.org").build();
	}

	@Override
	public void run(String... args) {
		this.client.get().uri("anything").retrieve().bodyToMono(Data.class).subscribe(message -> logger.info(message.toString()));
	}
	
}