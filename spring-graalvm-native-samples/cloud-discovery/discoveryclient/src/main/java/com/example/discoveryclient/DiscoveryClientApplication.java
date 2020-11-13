package com.example.discoveryclient;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableDiscoveryClient
public class DiscoveryClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscoveryClientApplication.class, args);
		try { Thread.sleep(20000); } catch (Exception e) {}
	}
}

@RestController
class Foo {
	@GetMapping("/")
	public String bar() { return "bar";}
	
}

@Configuration
class CLR implements CommandLineRunner {

	@Override
	public void run(String... args) throws Exception {
		System.out.println("Foo");
	}

}
