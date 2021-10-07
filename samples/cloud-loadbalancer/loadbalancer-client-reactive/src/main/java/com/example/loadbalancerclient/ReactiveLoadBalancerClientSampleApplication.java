package com.example.loadbalancerclient;

import java.net.URI;

import reactor.core.publisher.Mono;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@RestController
public class ReactiveLoadBalancerClientSampleApplication {

	private final WebClient webClient;

	public ReactiveLoadBalancerClientSampleApplication(WebClient.Builder webClientBuilder) {
		webClient = webClientBuilder.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(ReactiveLoadBalancerClientSampleApplication.class, args);
	}

	@GetMapping("/")
	public Mono<String> testService() {
		return webClient.get().uri(URI.create("http://test-service")).retrieve()
				.bodyToMono(String.class);
	}

	@GetMapping("/custom")
	public Mono<String> customTestService() {
		return webClient.get().uri(URI.create("http://custom-test-service")).retrieve()
				.bodyToMono(String.class);
	}

}
