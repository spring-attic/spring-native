/*
 * Copyright 2021-2022 the original author or authors.
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

package com.example.batch.configuration;

import javax.sql.DataSource;

import com.example.batch.ItemReaderListener;
import com.example.batch.domain.Person;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Michael Minella
 * @author Mahmoud Ben Hassine
 */
@Configuration(proxyBeanMethods = false)
public class BatchConfiguration {

	private static final Log logger = LogFactory.getLog(BatchConfiguration.class);

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job job(Step step1, Step step2) {
		return this.jobBuilderFactory.get("job")
				.start(step1)
				.next(step2)
				.build();
	}

	@StepScope
	@Bean
	// We advice users to return the concrete implementation for use cases like this where a bean is Step or Job scoped
	// and implement multiple interfaces.
	// TODO See https://github.com/spring-projects-experimental/spring-native/issues/459
	public ItemReaderListener reader() {
		ItemReaderListener itemReaderListener = new ItemReaderListener();
		itemReaderListener.setName("reader");
		itemReaderListener.setResource(new ClassPathResource("person.csv"));
		itemReaderListener.setLineMapper(new DefaultLineMapper<>() {{
			setLineTokenizer(new DelimitedLineTokenizer() {{
				setNames("firstName", "lastName");
			}});
			setFieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
				setTargetType(Person.class);
			}});
		}});
		return itemReaderListener;
	}

	@JobScope
	@Bean
	public ItemProcessor<Person, Person> itemProcessor() {
		return person -> new Person(person.getFirstName(), person.getLastName().toUpperCase());
	}

	@Bean
	public JdbcBatchItemWriter<Person> writer(DataSource dataSource) {
		return new JdbcBatchItemWriterBuilder<Person>()
				.dataSource(dataSource)
				.beanMapped()
				.sql("INSERT INTO PERSON VALUES (:firstName, :lastName)")
				.build();
	}

	@Bean
	public Step step1(ItemReaderListener itemReader,
			ItemProcessor<Person, Person> itemProcessor,
			ItemWriter<Person> itemWriter) {
		return this.stepBuilderFactory.get("step1")
				.<Person, Person>chunk(2)
				.reader(itemReader)
				.processor(itemProcessor)
				.writer(itemWriter)
				.listener(itemReader)
				.build();
	}

	@Bean
	public Step step2() {
		return this.stepBuilderFactory.get("step2")
				.tasklet((stepContribution, chunkContext) -> {
					System.out.println("Batch IO ran!");
					logger.info("INFO log message");
					return RepeatStatus.FINISHED;
				}).build();
	}
}
