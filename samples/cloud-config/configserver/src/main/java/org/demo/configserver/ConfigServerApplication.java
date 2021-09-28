package org.demo.configserver;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

	@Bean
	public CommandLineRunner clientCommandLineRunner() {
		return args -> System.out.println("Foo");
	}

	public static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args);
	}

}
