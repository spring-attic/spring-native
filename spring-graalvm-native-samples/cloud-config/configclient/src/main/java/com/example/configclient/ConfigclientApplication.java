package com.example.configclient;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
public class ConfigclientApplication {


	public static void main(String[] args) {
		SpringApplication.run(ConfigclientApplication.class, args);
		try { Thread.sleep(20000); } catch (Exception e) {}
	}
}

@Configuration
class CLR implements CommandLineRunner {

	@Value("${my.prop}")
	String injected;

	@Override
	public void run(String... args) throws Exception {
		System.out.println(":" + injected);
	}

}
