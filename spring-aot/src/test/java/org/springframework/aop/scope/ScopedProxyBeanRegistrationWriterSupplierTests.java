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

package org.springframework.aop.scope;

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.bean.BeanRegistrationWriter;
import org.springframework.aot.context.bootstrap.generator.infrastructure.BootstrapClass;
import org.springframework.aot.context.bootstrap.generator.infrastructure.BootstrapWriterContext;
import org.springframework.aot.context.bootstrap.generator.sample.factory.NumberHolder;
import org.springframework.aot.context.bootstrap.generator.test.CodeSnippet;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.samples.simple.SimpleComponent;
import org.springframework.core.ResolvableType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ScopedProxyBeanRegistrationWriterSupplier}.
 *
 * @author Stephane Nicoll
 */
class ScopedProxyBeanRegistrationWriterSupplierTests {

	@Test
	void getWithNonScopedProxy() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(PropertiesFactoryBean.class)
				.getBeanDefinition();
		assertThat(getBeanRegistrationWriter(beanFactory, "test", beanDefinition)).isNull();
	}

	@Test
	void getWithScopedProxyWithoutTargetBeanName() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		BeanDefinition scopeBean = BeanDefinitionBuilder.rootBeanDefinition(ScopedProxyFactoryBean.class)
				.getBeanDefinition();
		assertThat(getBeanRegistrationWriter(beanFactory, "test", scopeBean)).isNull();
	}

	@Test
	void getWithScopedProxyWithInvalidTargetBeanName() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		BeanDefinition scopeBean = BeanDefinitionBuilder.rootBeanDefinition(ScopedProxyFactoryBean.class)
				.addPropertyValue("targetBeanName", "testDoesNotExist").getBeanDefinition();
		assertThat(getBeanRegistrationWriter(beanFactory, "test", scopeBean)).isNull();
	}

	@Test
	void getWithScopedProxyWithTargetBeanName() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		BeanDefinition targetBean = BeanDefinitionBuilder.rootBeanDefinition(SimpleComponent.class)
				.getBeanDefinition();
		beanFactory.registerBeanDefinition("simpleComponent", targetBean);
		BeanDefinition scopeBean = BeanDefinitionBuilder.rootBeanDefinition(ScopedProxyFactoryBean.class)
				.addPropertyValue("targetBeanName", "simpleComponent").getBeanDefinition();
		assertThat(getBeanRegistrationWriter(beanFactory, "test", scopeBean)).isNotNull();
	}

	@Test
	void writeBeanRegistrationForScopedProxy() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		RootBeanDefinition targetBean = new RootBeanDefinition();
		targetBean.setTargetType(ResolvableType.forClassWithGenerics(NumberHolder.class, Integer.class));
		targetBean.setScope("custom");
		beanFactory.registerBeanDefinition("numberHolder", targetBean);
		BeanDefinition scopeBean = BeanDefinitionBuilder.rootBeanDefinition(ScopedProxyFactoryBean.class)
				.addPropertyValue("targetBeanName", "numberHolder").getBeanDefinition();
		assertThat(writeBeanRegistration(beanFactory, "test", scopeBean)).lines().containsOnly(
				"BeanDefinitionRegistrar.of(\"test\", ResolvableType.forClassWithGenerics(NumberHolder.class, Integer.class))",
				"    .instanceSupplier(() ->  {",
				"      ScopedProxyFactoryBean factory = new ScopedProxyFactoryBean();",
				"      factory.setTargetBeanName(\"numberHolder\");",
				"      factory.setBeanFactory(context.getBeanFactory());",
				"      return factory.getObject();",
				"    }).register(context);");
	}

	private CodeSnippet writeBeanRegistration(BeanFactory beanFactory, String beanName, BeanDefinition beanDefinition) {
		return CodeSnippet.of((code) -> getBeanRegistrationWriter(beanFactory, beanName, beanDefinition)
				.writeBeanRegistration(new BootstrapWriterContext(BootstrapClass.of("com.example")), code));
	}

	BeanRegistrationWriter getBeanRegistrationWriter(BeanFactory beanFactory, String beanName, BeanDefinition beanDefinition) {
		ScopedProxyBeanRegistrationWriterSupplier supplier = new ScopedProxyBeanRegistrationWriterSupplier();
		supplier.setBeanFactory(beanFactory);
		return supplier.get(beanName, beanDefinition);
	}

}
