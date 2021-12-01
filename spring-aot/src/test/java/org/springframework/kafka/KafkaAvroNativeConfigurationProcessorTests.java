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

package org.springframework.kafka;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.avro.specific.AvroGenerated;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.DefaultNativeReflectionEntry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link KafkaAvroNativeConfigurationProcessor}.
 *
 * @author Gary Russell
 */
public class KafkaAvroNativeConfigurationProcessorTests {

	@Test
	void genericMessageListener() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class)
				.getBeanDefinition());
		beanFactory.registerBeanDefinition("myListener", BeanDefinitionBuilder
				.rootBeanDefinition(MyListener.class)
				.getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		assertThat(registry.reflection().reflectionEntries()).singleElement().satisfies((entry) ->
				assertThat(entry.getType()).isSameAs(AvroType1.class));
	}

	@Test
	void kafkaListenerSimple() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class)
				.getBeanDefinition());
		beanFactory.registerBeanDefinition("myListener", BeanDefinitionBuilder
				.rootBeanDefinition(Listener1.class)
				.getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		assertThat(registry.reflection().reflectionEntries()).singleElement().satisfies((entry) ->
				assertThat(entry.getType()).isSameAs(AvroType1.class));
	}

	@Test
	void kafkaListenerListOfAvros() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class)
				.getBeanDefinition());
		beanFactory.registerBeanDefinition("myListener", BeanDefinitionBuilder
				.rootBeanDefinition(Listener2.class)
				.getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		assertThat(registry.reflection().reflectionEntries()).singleElement().satisfies((entry) ->
				assertThat(entry.getType()).isSameAs(AvroType1.class));
	}

	@Test
	void kafkaListenerConsumerRecordTwoAvros() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class)
				.getBeanDefinition());
		beanFactory.registerBeanDefinition("myListener", BeanDefinitionBuilder
				.rootBeanDefinition(Listener3.class)
				.getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		List<DefaultNativeReflectionEntry> entries = registry.reflection().reflectionEntries().collect(Collectors.toList());
		assertThat(entries).anySatisfy((entry) -> {
			assertThat(entry.getType()).isEqualTo(AvroType1.class);
			assertThat(entry.getAccess()).containsOnly(TypeAccess.DECLARED_CONSTRUCTORS);
		});
		assertThat(entries).anySatisfy((entry) -> {
			assertThat(entry.getType()).isEqualTo(AvroType2.class);
			assertThat(entry.getAccess()).containsOnly(TypeAccess.DECLARED_CONSTRUCTORS);
		});
		assertThat(entries).hasSize(2);
	}

	@Test
	void kafkaListenerConsumerRecordsTwoAvros() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class)
				.getBeanDefinition());
		beanFactory.registerBeanDefinition("myListener", BeanDefinitionBuilder
				.rootBeanDefinition(Listener4.class)
				.getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		List<DefaultNativeReflectionEntry> entries = registry.reflection().reflectionEntries().collect(Collectors.toList());
		assertThat(entries).anySatisfy((entry) -> {
			assertThat(entry.getType()).isEqualTo(AvroType1.class);
		});
		assertThat(entries).anySatisfy((entry) -> {
			assertThat(entry.getType()).isEqualTo(AvroType2.class);
		});
		assertThat(entries).hasSize(2);
	}

	@Test
	void notAComponent() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class)
				.getBeanDefinition());
		beanFactory.registerBeanDefinition("myListener", BeanDefinitionBuilder
				.rootBeanDefinition(NotAComponentListener5.class)
				.getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		assertThat(registry.reflection().reflectionEntries()).singleElement().satisfies((entry) ->
				assertThat(entry.getType()).isSameAs(AvroType3.class));
	}

	private NativeConfigurationRegistry process(DefaultListableBeanFactory beanFactory) {
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new KafkaAvroNativeConfigurationProcessor().process(beanFactory, registry);
		return registry;
	}

	@Component
	static class MyListener implements MessageListener<String, AvroType1> {

		@Override
		public void onMessage(ConsumerRecord<String, AvroType1> record) {
		}

	}

	@Component
	static class Listener1 {

		@KafkaListener
		void listen(AvroType1 avro) {
		}

	}

	@Component
	static class Listener2 {

		@KafkaListener
		void listen(List<AvroType1> avro) {
		}

	}

	@Component
	static class Listener3 {

		@KafkaListener
		void listen(ConsumerRecord<AvroType1, AvroType2> record) {
		}

	}

	@Component
	static class Listener4 {

		@KafkaListener
		void listen(ConsumerRecords<AvroType1, AvroType2> record) {
		}

	}

	static class NotAComponentListener5 {

		@KafkaListener
		void listen(AvroType3 avro) {
		}

	}

	@AvroGenerated
	private static final class AvroType1 {
	}

	@AvroGenerated
	private static final class AvroType2 {
	}

	@AvroGenerated
	private static final class AvroType3 {
	}

}
