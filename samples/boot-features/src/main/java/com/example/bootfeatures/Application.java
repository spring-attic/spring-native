package com.example.bootfeatures;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Application {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(Application.class, args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}

	@Bean
	public RestTemplate foo() {
		return new RestTemplateBuilder().requestFactory(() -> new FooClientHttpRequestFactory()).build();
	}
	
}
