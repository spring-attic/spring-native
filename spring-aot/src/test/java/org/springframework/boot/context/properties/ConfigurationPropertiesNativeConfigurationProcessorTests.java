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

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeReflectionEntry;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationPropertiesNativeConfigurationProcessorTests.SamplePropertiesWithNested.OneLevelDown;
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
	void processConfigurationProperties() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("beanA", BeanDefinitionBuilder.rootBeanDefinition(SampleProperties.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("beanB", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		assertThat(registry.reflection().getEntries()).singleElement().satisfies(allDeclaredMethods(SampleProperties.class));
	}

	@Test
	void processConfigurationPropertiesWithNestedType() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("beanA", BeanDefinitionBuilder.rootBeanDefinition(SamplePropertiesWithNested.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("beanB", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		assertThat(registry.reflection().getEntries()).anySatisfy(allDeclaredMethods(SamplePropertiesWithNested.class))
				.anySatisfy(allDeclaredMethods(OneLevelDown.class)).hasSize(2);
	}

	@Test
	void processConfigurationPropertiesWithNestedExternalType() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("beanA", BeanDefinitionBuilder.rootBeanDefinition(SamplePropertiesWithExternalNested.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("beanB", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		assertThat(registry.reflection().getEntries()).anySatisfy(allDeclaredMethods(SamplePropertiesWithExternalNested.class))
				.anySatisfy(allDeclaredMethods(SampleType.class)).anySatisfy(allDeclaredMethods(SampleType.Nested.class)).hasSize(3);
	}

	private Consumer<NativeReflectionEntry> allDeclaredMethods(Class<?> type) {
		return (entry) -> {
			assertThat(entry.getType()).isEqualTo(type);
			assertThat(entry.getFlags()).containsOnly(Flag.allDeclaredMethods, Flag.allDeclaredConstructors);
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

		static class Nested {

		}

	}

}
