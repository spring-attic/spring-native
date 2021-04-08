package com.example.quartznative;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Indexed;

import java.time.Instant;

/**
 *
 * You need to make the class of the {@link Job} itself available for reflection.
 *
 * There are two options here:
 *
 * <OL>
 * <LI>use @Indexed on the class itself (see below)</LI>
 * <LI>declare a @TypeHint()</LI>
 *
 * </OL>
 *
 */
// @org.springframework.nativex.hint.TypeHint(types = SampleJob.class)
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
	//

}

@Indexed
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