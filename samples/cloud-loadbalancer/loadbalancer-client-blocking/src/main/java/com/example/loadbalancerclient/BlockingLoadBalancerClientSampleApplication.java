package com.example.loadbalancerclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@RestController
public class BlockingLoadBalancerClientSampleApplication {

	@Autowired
	RestTemplate restTemplate;

	public static void main(String[] args) {
		SpringApplication.run(BlockingLoadBalancerClientSampleApplication.class, args);
	}

	@GetMapping("/")
	public String testService() {
		return restTemplate.getForObject("http://test-service", String.class);
	}

	@GetMapping("/custom")
	public String customTestService() {
		return restTemplate.getForObject("http://custom-test-service", String.class);
	}

}
