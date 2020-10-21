package com.example.commandlinerunner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CommandlinerunnerApplication {

	public static void main(String[] args) throws InterruptedException {
		new SpringApplicationBuilder(CommandlinerunnerApplication.class)
				// This could be extracted into an entry in spring.factories
				.initializers(context -> context.addBeanFactoryPostProcessor(new LiteConfigurationPostProcessor())) //
				.run(args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}

	@Bean
	public CommandLineRunner runner() {
		return args -> System.err.println("Hello World");
	}

}
