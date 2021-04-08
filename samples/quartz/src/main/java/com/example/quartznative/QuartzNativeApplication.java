/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.quartznative;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.time.Instant;

/**
 * 
 * @author Josh Long
 */
@SpringBootApplication
public class QuartzNativeApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuartzNativeApplication.class, args);
	}

	@Bean
	JobDetail sampleJobDetail(@Value("${greetings.name:World}") String name) {
		return JobBuilder.newJob(SampleJob.class) //
				.withIdentity("sampleJob") //
				.usingJobData("name", name) //
				.storeDurably() //
				.build();
	}

	@Bean
	Trigger sampleJobTrigger(JobDetail sampleJobDetail) {
		SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule() //
				.withIntervalInSeconds(2) //
				.withRepeatCount(5);

		return TriggerBuilder//
				.newTrigger()//
				.forJob(sampleJobDetail)//
				.withIdentity("sampleTrigger") //
				.withSchedule(scheduleBuilder) //
				.build();
	}

}

class SampleJob extends QuartzJobBean {

	private String name;

	// Invoked if a Job data map entry with that name
	public void setName(String name) {
		this.name = name;
	}

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		System.out.println(String.format("Hello %s @ " + Instant.now() + "!", this.name));
	}

}