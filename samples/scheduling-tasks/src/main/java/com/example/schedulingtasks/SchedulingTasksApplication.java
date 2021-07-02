package com.example.schedulingtasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SchedulingTasksApplication {
	
	@Autowired
	ScheduledTasksNonComponent scheduledTasks;

	public static void main(String[] args) {
		SpringApplication.run(SchedulingTasksApplication.class);
	}
	
	@Bean
	ScheduledTasksNonComponent getTask() {
		return new ScheduledTasksNonComponent();
	}
}
