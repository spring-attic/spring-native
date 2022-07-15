package com.example.demo;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringCloudStreamKafkaApplication {

	@Autowired
	private StreamBridge streamBridge;

	public static void main(String[] args) {
		SpringApplication.run(SpringCloudStreamKafkaApplication.class, args);
	}

	@Bean
	public Function<String, String> graalUppercaseFunction() {
		return String::toUpperCase;
	}

	@Bean
	public Consumer<String> graalLoggingConsumer() {
		return s -> {
			System.out.println("++++++Received:" + s);
			// Verifying that StreamBridge API works in native applications.
			streamBridge.send("sb-out", s);
		};
	}

	@Bean
	public Supplier<String> graalSupplier() {
		return () -> {
			String woodchuck = "How much wood could a woodchuck chuck if a woodchuck could chuck wood?";
			final String[] splitWoodchuck = woodchuck.split(" ");
			Random random = new Random();
			return splitWoodchuck[random.nextInt(splitWoodchuck.length)];
		};
	}
}
