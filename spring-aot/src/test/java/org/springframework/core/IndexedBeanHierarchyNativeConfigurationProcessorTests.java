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

package org.springframework.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Tests for {@link IndexedBeanHierarchyNativeConfigurationProcessor}.
 *
 * @author Andy Clement
 */
class IndexedBeanHierarchyNativeConfigurationProcessorTests {

	@Test
	void basicComponent() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(Bar.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		List<ClassDescriptor> classDescriptors = registry.reflection().toClassDescriptors();
		assertThat(classDescriptors).hasSize(4);
		assertThat(classDescriptors).contains(getClassDescriptor(Foo.class,TypeAccess.DECLARED_METHODS));
		assertThat(classDescriptors).contains(getClassDescriptor(Boo.class,TypeAccess.DECLARED_METHODS));
		assertThat(classDescriptors).contains(getClassDescriptor(Bar.class,TypeAccess.DECLARED_METHODS));
		// TODO maybe filter out the java.* types in the configuration processor
		assertThat(classDescriptors).contains(getClassDescriptor(Object.class,TypeAccess.DECLARED_METHODS));
	}
	
	@Test
	void properties() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(SomeProperties.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		List<ClassDescriptor> classDescriptors = registry.reflection().toClassDescriptors();
		assertThat(classDescriptors).hasSize(1);
		assertThat(classDescriptors).contains(getClassDescriptor(SomeProperties.class, 
				TypeAccess.DECLARED_METHODS, TypeAccess.DECLARED_CONSTRUCTORS,
				TypeAccess.DECLARED_CLASSES, TypeAccess.DECLARED_FIELDS));
	}
	
	private ClassDescriptor getClassDescriptor(Class<?> clazz, TypeAccess... accesses) {
		ClassDescriptor cd = ClassDescriptor.of(clazz);
		for (TypeAccess access: accesses) {
			cd.setAccess(access);
		}
		return cd;
	}

	private NativeConfigurationRegistry process(DefaultListableBeanFactory beanFactory) {
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new IndexedBeanHierarchyNativeConfigurationProcessor().process(beanFactory, registry);
		return registry;
	}
	
	@ConfigurationProperties
	@Validated
	static class SomeProperties {
	}

	interface Boo {
	}
	
	static class Foo {
	}

	@Component
	static class Bar extends Foo implements Boo {
	}
}

