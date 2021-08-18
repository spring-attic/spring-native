package com.example.task;

import org.springframework.boot.ApplicationRunner;
import org.springframework.aot.SpringAotApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableTask
public class TaskApplication {

	@Bean
	public ApplicationRunner applicationRunner() {

		return args -> System.out.println("Task ran!");
	}

	public static void main(String[] args) throws InterruptedException {
		SpringAotApplication.run(TaskApplication.class, args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}

}
