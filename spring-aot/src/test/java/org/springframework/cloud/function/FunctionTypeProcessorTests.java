/*
 * Copyright 2021-2021 the original author or authors.
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

package org.springframework.cloud.function;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.hint.TypeAccess;

/**
 *
 * @author Oleg Zhurakousky
 *
 */
class FunctionTypeProcessorTests {

	private static Set<TypeAccess> ALL_MEMBERS;
	{
		ALL_MEMBERS = new HashSet<>(Arrays.asList(TypeAccess.PUBLIC_FIELDS, TypeAccess.DECLARED_FIELDS, TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.DECLARED_METHODS, TypeAccess.PUBLIC_METHODS));
	}

	@Test
	void testFunctionTypes() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("sampleFunction", BeanDefinitionBuilder.rootBeanDefinition(MyFunction.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		List<ClassDescriptor> classDescriptors = registry.reflection().toClassDescriptors();
		assertThat(classDescriptors).hasSize(1);
		ClassDescriptor cd = classDescriptors.get(0);
		assertThat(cd.getName()).isEqualTo(Person.class.getName());
		assertThat(cd.getAccess()).containsAll(ALL_MEMBERS);
	}

	@Test
	void testConsumerTypes() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("sampleConsumer", BeanDefinitionBuilder.rootBeanDefinition(MyConsumer.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		List<ClassDescriptor> classDescriptors = registry.reflection().toClassDescriptors();
		assertThat(classDescriptors).hasSize(1);
		ClassDescriptor cd = classDescriptors.get(0);
		assertThat(cd.getName()).isEqualTo(Person.class.getName());
		assertThat(cd.getAccess()).containsAll(ALL_MEMBERS);
	}

	@Test
	void testSupplierTypes() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("sampleSupplier", BeanDefinitionBuilder.rootBeanDefinition(MySupplier.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		List<ClassDescriptor> classDescriptors = registry.reflection().toClassDescriptors();
		assertThat(classDescriptors).hasSize(0);
	}

	private NativeConfigurationRegistry process(DefaultListableBeanFactory beanFactory) {
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new FunctionTypeProcessor().process(beanFactory, registry);
		return registry;
	}

	public static class MyFunction implements Function<Person, String> {
		@Override
		public String apply(Person t) {
			return null;
		}
	}

	public static class MyConsumer implements Consumer<Person> {
		@Override
		public void accept(Person t) {
		}
	}

	public static class MySupplier implements Supplier<Person> {
		@Override
		public Person get() {
			return null;
		}
	}

	public static class Person {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}
