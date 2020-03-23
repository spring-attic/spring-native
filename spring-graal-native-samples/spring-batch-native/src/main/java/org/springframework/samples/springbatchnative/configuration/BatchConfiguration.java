/**
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.springbatchnative.configuration;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.samples.springbatchnative.domain.Person;

/**
 * @author Michael Minella
 */
@EnableBatchProcessing
@Configuration
public class BatchConfiguration {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job job() {
		return this.jobBuilderFactory.get("job")
				.start(step1())
				.build();
	}

	@Bean
	public Step step1() {
		return this.stepBuilderFactory.get("step1")
				.<Person, Person>chunk(1)
				.reader(reader(null))
				.writer(writer())
				.build();
	}

	@Bean
	@StepScope
	public FlatFileItemReader<Person> reader(@Value("#{jobParameters['inputFile']}") Resource resource) {
		return new FlatFileItemReaderBuilder<Person>()
				.name("flatFileItemReader")
				.resource(resource)
				.linesToSkip(1)
				.delimited()
				.names("firstName", "lastName", "address", "city", "state", "zip")
				.targetType(Person.class)
				.build();
	}

	@Bean
	@StepScope
	public CompositeItemWriter<Person> writer() {
		return new CompositeItemWriterBuilder<Person>()
				.delegates(items -> {
					for (Person item : items) {
						System.out.println(">> item = " + item);
					}
				}, jdbcWriter(null))
				.build();
	}

	@Bean
	@StepScope
	public JdbcBatchItemWriter<Person> jdbcWriter(DataSource dataSource) {
		return new JdbcBatchItemWriterBuilder<Person>()
				.dataSource(dataSource)
				.sql("INSERT INTO PERSON (first_name, last_name, address, city, state, zip) " +
						"VALUES (:firstName, :lastName, :address, :city, :state, :zip)")
				.beanMapped()
				.build();
	}
}
