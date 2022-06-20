package com.example.batch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@EnableBatchProcessing
@SpringBootApplication
// TODO: Move the hints for Hikari from META-INF/native-image to graalvm-reachability-metadata
public class BatchApplication {

	private static final Log logger = LogFactory.getLog(BatchApplication.class);

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job job(Step step1) {
		return this.jobBuilderFactory.get("job")
				.start(step1)
				.build();
	}

	@Bean
	public Step step1() {
		return this.stepBuilderFactory.get("step1")
				.tasklet((stepContribution, chunkContext) -> {
					System.out.println("Batch ran!");
					logger.info("INFO log message");
					return RepeatStatus.FINISHED;
				}).build();
	}

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(BatchApplication.class, args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}

}
