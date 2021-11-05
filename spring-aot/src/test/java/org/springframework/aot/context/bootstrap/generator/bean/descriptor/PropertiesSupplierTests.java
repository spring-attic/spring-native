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

package org.springframework.aot.context.bootstrap.generator.bean.descriptor;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.PropertyDescriptor;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PropertiesSupplier}.
 *
 * @author Stephane Nicoll
 */
class PropertiesSupplierTests {

	@Test
	void detectPropertiesOnBeanDefinitionWithNoProperties() {
		assertThat(detect(BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition())).isEmpty();
	}

	@Test
	void detectPropertiesWithValidProperty() {
		assertThat(detect(BeanDefinitionBuilder.rootBeanDefinition(TestComponent.class)
				.addPropertyValue("name", "Hello").getBeanDefinition())).singleElement().satisfies((descriptor) -> {
			assertThat(descriptor.getWriteMethod()).isNotNull().
					isEqualTo(ReflectionUtils.findMethod(TestComponent.class, "setName", String.class));
			PropertyValue propertyValue = descriptor.getPropertyValue();
			assertThat(propertyValue.getName()).isEqualTo("name");
			assertThat(propertyValue.getValue()).isEqualTo("Hello");
		});
	}

	@Test
	void detectPropertiesWithValidBuilderStyleProperty() {
		assertThat(detect(BeanDefinitionBuilder.rootBeanDefinition(BuilderStyleComponent.class)
				.addPropertyValue("name", "Hello").getBeanDefinition())).singleElement().satisfies((descriptor) -> {
			assertThat(descriptor.getWriteMethod()).isNotNull().
					isEqualTo(ReflectionUtils.findMethod(BuilderStyleComponent.class, "setName", String.class));
			PropertyValue propertyValue = descriptor.getPropertyValue();
			assertThat(propertyValue.getName()).isEqualTo("name");
			assertThat(propertyValue.getValue()).isEqualTo("Hello");
		});
	}

	@Test
	void detectPropertiesWithSeveralProperties() {
		List<PropertyDescriptor> properties = detect(BeanDefinitionBuilder.rootBeanDefinition(TestComponent.class)
				.addPropertyValue("name", "Hello").addPropertyValue("counter", 42).getBeanDefinition());
		assertThat(properties).hasSize(2);
		assertThat(properties).anySatisfy((descriptor) -> {
			assertThat(descriptor.getWriteMethod()).isNotNull().
					isEqualTo(ReflectionUtils.findMethod(TestComponent.class, "setName", String.class));
			PropertyValue propertyValue = descriptor.getPropertyValue();
			assertThat(propertyValue.getName()).isEqualTo("name");
			assertThat(propertyValue.getValue()).isEqualTo("Hello");
		});
		assertThat(properties).anySatisfy((descriptor) -> {
			assertThat(descriptor.getWriteMethod()).isNotNull().
					isEqualTo(ReflectionUtils.findMethod(TestComponent.class, "setCounter", Integer.class));
			PropertyValue propertyValue = descriptor.getPropertyValue();
			assertThat(propertyValue.getName()).isEqualTo("counter");
			assertThat(propertyValue.getValue()).isEqualTo(42);
		});
	}

	@Test
	void detectPropertiesWithUnknownProperty() {
		assertThat(detect(BeanDefinitionBuilder.rootBeanDefinition(TestComponent.class)
				.addPropertyValue("unknown", "Hello").getBeanDefinition())).singleElement().satisfies((descriptor) -> {
			assertThat(descriptor.getWriteMethod()).isNull();
			PropertyValue propertyValue = descriptor.getPropertyValue();
			assertThat(propertyValue.getName()).isEqualTo("unknown");
			assertThat(propertyValue.getValue()).isEqualTo("Hello");
		});
	}

	@Test
	void detectPropertiesWithInvalidWriteMethod() {
		assertThat(detect(BeanDefinitionBuilder.rootBeanDefinition(InvalidPropertyComponent.class)
				.addPropertyValue("name", "Hello").getBeanDefinition())).singleElement().satisfies((descriptor) -> {
			assertThat(descriptor.getWriteMethod()).isNull();
			PropertyValue propertyValue = descriptor.getPropertyValue();
			assertThat(propertyValue.getName()).isEqualTo("name");
			assertThat(propertyValue.getValue()).isEqualTo("Hello");
		});
	}

	@Test
	void detectPropertiesWithNoWriteMethod() {
		assertThat(detect(BeanDefinitionBuilder.rootBeanDefinition(ReadOnlyProperty.class)
				.addPropertyValue("name", "Hello").getBeanDefinition())).singleElement()
				.satisfies((descriptor) -> assertThat(descriptor.getWriteMethod()).isNull());
	}

	private List<PropertyDescriptor> detect(AbstractBeanDefinition beanDefinition) {
		PropertiesSupplier supplier = new PropertiesSupplier();
		return supplier.detectProperties(beanDefinition);
	}


	@SuppressWarnings("unused")
	static class TestComponent {

		private String name;

		private Integer counter;

		public void setName(String name) {
			this.name = name;
		}

		public void setCounter(Integer counter) {
			this.counter = counter;
		}
	}

	@SuppressWarnings("unused")
	static class BuilderStyleComponent {

		private String name;

		public BuilderStyleComponent setName(String name) {
			this.name = name;
			return this;
		}
	}

	@SuppressWarnings("unused")
	static class InvalidPropertyComponent {

		private String name;

		void setName(String name) {
			this.name = name;
		}

	}

	static class ReadOnlyProperty {

		private String name;

		public String getName() {
			return this.name;
		}

	}
}
