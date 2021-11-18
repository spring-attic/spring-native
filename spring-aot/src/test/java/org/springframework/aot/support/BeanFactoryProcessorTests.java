/*
 * Copyright 2019-2021 the original author or authors.
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

package org.springframework.aot.support;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.sample.factory.TestGenericFactoryBeanConfiguration;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.BuildTimeBeanDefinitionsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.samples.scan.ScanConfiguration;
import org.springframework.context.annotation.samples.simple.ConfigurationOne;
import org.springframework.context.annotation.samples.simple.ConfigurationTwo;
import org.springframework.context.support.GenericApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * Tests for {@link BeanFactoryProcessor}.
 *
 * @author Stephane Nicoll
 */
class BeanFactoryProcessorTests {

	@Test
	void processWithMatchingType() {
		ListableBeanFactory beanFactory = prepare(ConfigurationOne.class);
		ConsumerCollector consumer = new ConsumerCollector();
		new BeanFactoryProcessor(beanFactory).processBeansWithType(String.class, consumer);
		assertThat(consumer.callbacks).containsOnly(entry("beanOne", String.class));
	}

	@Test
	void processWithMatchingTypeAndSeveralCandidates() {
		ListableBeanFactory beanFactory = prepare(ScanConfiguration.class);
		ConsumerCollector consumer = new ConsumerCollector();
		new BeanFactoryProcessor(beanFactory).processBeansWithType(String.class, consumer);
		assertThat(consumer.callbacks).containsOnly(entry("beanOne", String.class),
				entry("beanTwo", String.class));
	}

	@Test
	void processWithAllFiltersNullBeanType() {
		ListableBeanFactory beanFactory = prepare(ConfigurationOne.class, TestGenericFactoryBeanConfiguration.class);
		ConsumerCollector consumer = new ConsumerCollector();
		new BeanFactoryProcessor(beanFactory).processBeansWithType(null, consumer);
		assertThat(consumer.callbacks).doesNotContainKey("testGenericFactoryBean");
	}

	@Test
	void processWithMatchingAnnotation() {
		ListableBeanFactory beanFactory = prepare(ConfigurationOne.class);
		ConsumerCollector consumer = new ConsumerCollector();
		new BeanFactoryProcessor(beanFactory).processBeansWithAnnotation(Configuration.class, consumer);
		assertThat(consumer.callbacks).containsOnly(entry("configurationOne", ConfigurationOne.class));
	}

	@Test
	void processWithMatchingAnnotationAndSeveralCandidates() {
		ListableBeanFactory beanFactory = prepare(ScanConfiguration.class);
		ConsumerCollector consumer = new ConsumerCollector();
		new BeanFactoryProcessor(beanFactory).processBeansWithAnnotation(Configuration.class, consumer);
		assertThat(consumer.callbacks).containsOnly(
				entry("scanConfiguration", ScanConfiguration.class),
				entry("configurationOne", ConfigurationOne.class),
				entry("configurationTwo", ConfigurationTwo.class));
	}


	private ListableBeanFactory prepare(Class<?>... candidates) {
		GenericApplicationContext context = new AnnotationConfigApplicationContext();
		for (Class<?> candidate : candidates) {
			context.registerBean(candidate);
		}
		return new BuildTimeBeanDefinitionsRegistrar().processBeanDefinitions(context);
	}

	static class ConsumerCollector implements BiConsumer<String, Class<?>> {

		private final Map<String, Class<?>> callbacks = new LinkedHashMap<>();

		@Override
		public void accept(String beanName, Class<?> beanType) {
			this.callbacks.put(beanName, beanType);
		}

	}

}
