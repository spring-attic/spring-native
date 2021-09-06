package com.example.discoveryclient;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableDiscoveryClient
public class DiscoveryClientApplication {

	@Bean
	public CommandLineRunner clientCommandLineRunner() {
		return args -> 	System.out.println("Foo");
	}

	public static void main(String[] args) {
		SpringApplication.run(DiscoveryClientApplication.class, args);
		try { Thread.sleep(20000); } catch (Exception e) {}
	}

	@RestController
	static class Foo {
		@GetMapping("/")
		public String bar() { return "bar";}

	}
}
