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

package org.springframework.boot.context.properties;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.DefaultNativeReflectionEntry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.nativex.hint.Flag;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ConfigurationPropertiesNativeConfigurationProcessor}.
 *
 * @author Stephane Nicoll
 * @author Christoph Strobl
 */
class ConfigurationPropertiesNativeConfigurationProcessorTests {

	@Test
	void processJavaBeanConfigurationProperties() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("beanA", BeanDefinitionBuilder
				.rootBeanDefinition(SampleProperties.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("beanB", BeanDefinitionBuilder
				.rootBeanDefinition(String.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		assertThat(registry.reflection().reflectionEntries()).singleElement()
				.satisfies(javaBeanBinding(SampleProperties.class));
	}

	@Test
	void processJavaBeanConfigurationPropertiesWithSeveralConstructors() throws NoSuchMethodException {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("beanA", BeanDefinitionBuilder
				.rootBeanDefinition(SamplePropertiesWithSeveralConstructors.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		assertThat(registry.reflection().reflectionEntries()).singleElement()
				.satisfies(javaBeanBinding(SamplePropertiesWithSeveralConstructors.class,
						SamplePropertiesWithSeveralConstructors.class.getDeclaredConstructor()));
	}

	@Test
	void processJavaBeanConfigurationPropertiesWithMapOfPojo() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("beanA", BeanDefinitionBuilder
				.rootBeanDefinition(SamplePropertiesWithMap.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		List<DefaultNativeReflectionEntry> entries = registry.reflection().reflectionEntries().collect(Collectors.toList());
		assertThat(entries).anySatisfy(javaBeanBinding(SamplePropertiesWithMap.class));
		assertThat(entries).anySatisfy(javaBeanBinding(Address.class));
		assertThat(entries).hasSize(2);
	}

	@Test
	void processJavaBeanConfigurationPropertiesWithListOfPojo() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("beanA", BeanDefinitionBuilder
				.rootBeanDefinition(SamplePropertiesWithList.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		List<DefaultNativeReflectionEntry> entries = registry.reflection().reflectionEntries().collect(Collectors.toList());
		assertThat(entries).anySatisfy(javaBeanBinding(SamplePropertiesWithList.class));
		assertThat(entries).anySatisfy(javaBeanBinding(Address.class));
		assertThat(entries).hasSize(2);
	}

	@Test
	void processJavaBeanConfigurationPropertiesWitArrayOfPojo() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("beanA", BeanDefinitionBuilder
				.rootBeanDefinition(SamplePropertiesWithArray.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		List<DefaultNativeReflectionEntry> entries = registry.reflection().reflectionEntries().collect(Collectors.toList());
		assertThat(entries).anySatisfy(javaBeanBinding(SamplePropertiesWithArray.class));
		assertThat(entries).anySatisfy(javaBeanBinding(Address.class));
		assertThat(entries).hasSize(2);
	}

	@Test
	void processValueObjectConfigurationProperties() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("beanA", BeanDefinitionBuilder
				.rootBeanDefinition(SampleImmutableProperties.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("beanB", BeanDefinitionBuilder
				.rootBeanDefinition(String.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		assertThat(registry.reflection().reflectionEntries()).singleElement().satisfies(
				valueObjectBinding(SampleImmutableProperties.class,
						SampleImmutableProperties.class.getDeclaredConstructors()[0]));
	}

	@Test
	void processValueObjectConfigurationPropertiesWithSpecificConstructor() throws NoSuchMethodException {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("beanA", BeanDefinitionBuilder
				.rootBeanDefinition(SampleImmutablePropertiesWithSeveralConstructors.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		assertThat(registry.reflection().reflectionEntries()).singleElement().satisfies(
				valueObjectBinding(SampleImmutablePropertiesWithSeveralConstructors.class,
						SampleImmutablePropertiesWithSeveralConstructors.class.getDeclaredConstructor(String.class)));
	}

	@Test
	void processValueObjectConfigurationPropertiesWithSeveralLayersOfPojo() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("beanA", BeanDefinitionBuilder
				.rootBeanDefinition(SampleImmutablePropertiesWithList.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		List<DefaultNativeReflectionEntry> entries = registry.reflection().reflectionEntries().collect(Collectors.toList());
		assertThat(entries).anySatisfy(valueObjectBinding(SampleImmutablePropertiesWithList.class,
				SampleImmutablePropertiesWithList.class.getConstructors()[0]));
		assertThat(entries).anySatisfy(valueObjectBinding(Person.class, Person.class.getConstructors()[0]));
		assertThat(entries).anySatisfy(valueObjectBinding(Address.class, Address.class.getDeclaredConstructors()[0]));
		assertThat(entries).hasSize(3);
	}


	@Test
	void processConfigurationPropertiesWithNestedTypeNotUsedIsIgnored() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("beanA", BeanDefinitionBuilder.rootBeanDefinition(SamplePropertiesWithNested.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("beanB", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		assertThat(registry.reflection().reflectionEntries()).singleElement().satisfies(
				javaBeanBinding(SamplePropertiesWithNested.class));
	}

	@Test
	void processConfigurationPropertiesWithNestedExternalType() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("beanA", BeanDefinitionBuilder.rootBeanDefinition(SamplePropertiesWithExternalNested.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("beanB", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		assertThat(registry.reflection().reflectionEntries()).anySatisfy(javaBeanBinding(SamplePropertiesWithExternalNested.class))
				.anySatisfy(javaBeanBinding(SampleType.class)).anySatisfy(javaBeanBinding(SampleType.Nested.class)).hasSize(3);
	}

	private Consumer<DefaultNativeReflectionEntry> javaBeanBinding(Class<?> type) {
		return javaBeanBinding(type, type.getDeclaredConstructors()[0]);
	}

	private Consumer<DefaultNativeReflectionEntry> javaBeanBinding(Class<?> type, Constructor<?> constructor) {
		return (entry) -> {
			assertThat(entry.getType()).isEqualTo(type);
			assertThat(entry.getConstructors()).containsOnly(type.getDeclaredConstructors()[0]);
			assertThat(entry.getFlags()).containsOnly(Flag.allDeclaredMethods);
		};
	}

	private Consumer<DefaultNativeReflectionEntry> valueObjectBinding(Class<?> type, Constructor<?> constructor) {
		return (entry) -> {
			assertThat(entry.getType()).isEqualTo(type);
			assertThat(entry.getConstructors()).containsOnly(constructor);
			assertThat(entry.getFlags()).containsOnly(Flag.allDeclaredMethods);
		};
	}

	private NativeConfigurationRegistry process(DefaultListableBeanFactory beanFactory) {
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new ConfigurationPropertiesNativeConfigurationProcessor().process(beanFactory, registry);
		return registry;
	}


	@ConfigurationProperties("test")
	static class SampleProperties {

	}

	@ConfigurationProperties("test")
	static class SamplePropertiesWithSeveralConstructors {

		SamplePropertiesWithSeveralConstructors() {
		}

		public SamplePropertiesWithSeveralConstructors(String ignored) {
		}

	}

	@ConfigurationProperties("test")
	static class SamplePropertiesWithMap {

		public Map<String, Address> getAddresses() {
			return Collections.emptyMap();
		}
	}

	@ConfigurationProperties("test")
	static class SamplePropertiesWithList {

		public List<Address> getAllAddresses() {
			return Collections.emptyList();
		}

	}

	@ConfigurationProperties("test")
	static class SamplePropertiesWithArray {

		public Address[] getAllAddresses() {
			return new Address[0];
		}

	}

	@ConfigurationProperties
	@ConstructorBinding
	static class SampleImmutableProperties {

		private final String name;

		SampleImmutableProperties(String name) {
			this.name = name;
		}
	}

	@ConfigurationProperties
	@ConstructorBinding
	static class SampleImmutablePropertiesWithSeveralConstructors {

		private final String name;

		@ConstructorBinding
		SampleImmutablePropertiesWithSeveralConstructors(String name) {
			this.name = name;
		}

		public SampleImmutablePropertiesWithSeveralConstructors() {
			this("test");
		}

	}


	@ConfigurationProperties
	@ConstructorBinding
	static class SampleImmutablePropertiesWithList {

		private final List<Person> family;

		public SampleImmutablePropertiesWithList(List<Person> family) {
			this.family = family;
		}

	}

	@ConfigurationProperties("nested")
	static class SamplePropertiesWithNested {

		static class OneLevelDown {

		}
	}

	@ConfigurationProperties("nested")
	static class SamplePropertiesWithExternalNested {

		private String name;

		@NestedConfigurationProperty
		private SampleType sampleType;

		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public SampleType getSampleType() {
			return this.sampleType;
		}

		public void setSampleType(SampleType sampleType) {
			this.sampleType = sampleType;
		}

	}

	static class SampleType {

		private final Nested nested = new Nested();

		public Nested getNested() {
			return this.nested;
		}

		static class Nested {

		}

	}

	static class Address {


	}

	static class Person {
		private final String firstName;

		private final String lastName;

		@NestedConfigurationProperty
		private final Address address;

		public Person(String firstName, String lastName, Address address) {
			this.firstName = firstName;
			this.lastName = lastName;
			this.address = address;
		}

	}

}
