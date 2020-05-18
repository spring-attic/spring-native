/**
 * Copyright 2020-2020 the original author or authors.
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

package com.example.batch;

import com.example.domain.CustomerCredit;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.ResourceAwareItemWriterItemStream;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * @author Michael Minella
 */
@EnableBatchProcessing
@SpringBootApplication(proxyBeanMethods=false)
public class BatchConfiguration {

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

	// Must return the interface in order to prevent Objenesis proxy
	@Bean
	@StepScope
	public ItemStreamReader<CustomerCredit> itemReader() {
		return new FlatFileItemReaderBuilder<CustomerCredit>()
				.name("itemReader")
				.resource(new ClassPathResource("/customer.csv"))
				.delimited()
				.names("name", "credit")
				.targetType(CustomerCredit.class)
				.build();
	}

	@Bean
	@StepScope
	public ResourceAwareItemWriterItemStream<CustomerCredit> itemWriter(@Value("#{jobParameters[outputFile]}") Resource resource) {
		return new FlatFileItemWriterBuilder<CustomerCredit>()
				.name("itemWriter")
				.resource(resource)
				.delimited()
				.names("credit", "name")
				.build();
	}

	@Bean
	public Step step1(ItemReader<CustomerCredit> itemReader,
			ItemProcessor<CustomerCredit, CustomerCredit> itemProcessor,
			ItemWriter<CustomerCredit> itemWriter) {
		return this.stepBuilderFactory.get("step1")
				.<CustomerCredit, CustomerCredit>chunk(2)
				.reader(itemReader)
				.processor(itemProcessor)
				.writer(itemWriter)
				.build();
	}

	@Bean
	public CustomerCreditIncreaseProcessor itemProcessor() {
		return new CustomerCreditIncreaseProcessor();
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(BatchConfiguration.class, args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}
	
}
