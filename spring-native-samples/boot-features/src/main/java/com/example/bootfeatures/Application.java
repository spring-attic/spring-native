package com.example.bootfeatures;

import com.example.bootfeatures.nestedcfg.School;
import com.example.bootfeatures.separatecfg.University;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({Creds.class,University.class,School.class,AcmeProperties.class})
public class Application {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(Application.class, args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}
	
}
