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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.sample.SimpleConfiguration;
import org.springframework.aot.context.bootstrap.generator.sample.injection.InjectionConfiguration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link DefaultBeanInstanceDescriptorFactory}.
 *
 * @author Stephane Nicoll
 */
class DefaultBeanInstanceDescriptorFactoryTests {

	@Test
	void createWithNullBeanDefinition() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		assertThatIllegalArgumentException().isThrownBy(() -> new DefaultBeanInstanceDescriptorFactory(beanFactory).create(null));
	}

	@Test
	@Disabled("Ignored for gh-1015")
	void createWithUnsupportedBeanDefinition() {
		BeanDefinition beanDefinition = new GenericBeanDefinition();
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		assertThat(new DefaultBeanInstanceDescriptorFactory(beanFactory).create(beanDefinition)).isNull();
	}

	@Test
	void createWithConstructor() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("test", BeanDefinitionBuilder.rootBeanDefinition(SimpleConfiguration.class).getBeanDefinition());
		BeanInstanceDescriptor descriptor = createDescriptor(beanFactory, "test");
		assertThat(descriptor.getUserBeanClass()).isEqualTo(SimpleConfiguration.class);
		assertThat(descriptor.getInstanceCreator()).isNotNull();
		assertThat(descriptor.getInstanceCreator().getMember()).isEqualTo(SimpleConfiguration.class.getDeclaredConstructors()[0]);
		assertThat(descriptor.getInjectionPoints()).isEmpty();
		assertThat(descriptor.getProperties()).isEmpty();
	}

	@Test
	void createWithInjectionPoints() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("test", BeanDefinitionBuilder.rootBeanDefinition(InjectionConfiguration.class).getBeanDefinition());
		BeanInstanceDescriptor descriptor = createDescriptor(beanFactory, "test");
		assertThat(descriptor.getUserBeanClass()).isEqualTo(InjectionConfiguration.class);
		assertThat(descriptor.getInstanceCreator()).isNotNull();
		assertThat(descriptor.getInstanceCreator().getMember()).isEqualTo(InjectionConfiguration.class.getDeclaredConstructors()[0]);
		assertThat(descriptor.getInjectionPoints()).hasSize(2);
		assertThat(descriptor.getInjectionPoints()).anySatisfy((injectionPoint) -> {
			assertThat(injectionPoint.getMember()).isEqualTo(ReflectionUtils.findMethod(InjectionConfiguration.class, "setEnvironment", Environment.class));
			assertThat(injectionPoint.isRequired()).isTrue();
		});
		assertThat(descriptor.getInjectionPoints()).anySatisfy((injectionPoint) -> {
			assertThat(injectionPoint.getMember()).isEqualTo(ReflectionUtils.findMethod(InjectionConfiguration.class, "setBean", String.class));
			assertThat(injectionPoint.isRequired()).isFalse();
		});
		assertThat(descriptor.getProperties()).isEmpty();
	}

	@Test
	void createWithProperties() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("test", BeanDefinitionBuilder.rootBeanDefinition(InjectionConfiguration.class)
				.addPropertyValue("name", "Hello").addPropertyValue("counter", 42).getBeanDefinition());
		BeanInstanceDescriptor descriptor = createDescriptor(beanFactory, "test");
		assertThat(descriptor.getProperties()).hasSize(2);
		assertThat(descriptor.getProperties()).anySatisfy((propertyDescriptor) -> {
			assertThat(propertyDescriptor.getWriteMethod()).isEqualTo(ReflectionUtils.findMethod(InjectionConfiguration.class, "setName", String.class));
			assertThat(propertyDescriptor.getPropertyValue().getName()).isEqualTo("name");
		});
		assertThat(descriptor.getProperties()).anySatisfy((propertyDescriptor) -> {
			assertThat(propertyDescriptor.getWriteMethod()).isEqualTo(ReflectionUtils.findMethod(InjectionConfiguration.class, "setCounter", Integer.class));
			assertThat(propertyDescriptor.getPropertyValue().getName()).isEqualTo("counter");
		});
	}

	private BeanInstanceDescriptor createDescriptor(DefaultListableBeanFactory beanFactory, String beanName) {
		return new DefaultBeanInstanceDescriptorFactory(beanFactory).create(beanFactory.getMergedBeanDefinition(beanName));
	}

}
