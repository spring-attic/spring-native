/*
 * Copyright 2019-2022 the original author or authors.
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

import java.io.IOException;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.infrastructure.BootstrapClass;
import org.springframework.aot.context.bootstrap.generator.infrastructure.BootstrapWriterContext;
import org.springframework.aot.context.bootstrap.generator.infrastructure.DefaultBootstrapWriterContext;
import org.springframework.aot.context.bootstrap.generator.test.CodeSnippet;
import org.springframework.aot.context.bootstrap.generator.test.TextAssert;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ConfigurationPropertiesBinderBeanRegistrationWriterSupplier}.
 *
 * @author Stephane Nicoll
 */
class ConfigurationPropertiesBinderBeanRegistrationWriterSupplierTests {

	private static final String BEAN_NAME = ConfigurationPropertiesBinderBeanRegistrationWriterSupplier.BEAN_NAME;

	private static final String FACTORY_BEAN_NAME = ConfigurationPropertiesBinderBeanRegistrationWriterSupplier.FACTORY_BEAN_NAME;

	private final ConfigurationPropertiesBinderBeanRegistrationWriterSupplier supplier = new ConfigurationPropertiesBinderBeanRegistrationWriterSupplier();

	@Test
	void getWithNonConfigurationPropertiesBinder() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(PropertiesFactoryBean.class)
				.getBeanDefinition();
		assertThat(this.supplier.get("test", beanDefinition)).isNull();
	}

	@Test
	void getWithConfigurationPropertiesBinder() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		ConfigurationPropertiesBinder.register(beanFactory);
		assertThat(beanFactory.containsBeanDefinition(BEAN_NAME)).isTrue();
		assertThat(this.supplier.get(BEAN_NAME, beanFactory.getMergedBeanDefinition(BEAN_NAME))).isNotNull();
	}

	@Test
	void filterConfigurationPropertiesBinderFactory() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		ConfigurationPropertiesBinder.register(beanFactory);
		assertThat(beanFactory.containsBeanDefinition(FACTORY_BEAN_NAME)).isTrue();
		assertThat(this.supplier.isExcluded(FACTORY_BEAN_NAME,
				beanFactory.getMergedBeanDefinition(FACTORY_BEAN_NAME))).isTrue();
	}

	@Test
	void writeBeanRegistrationForConfigurationPropertiesBinder() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		ConfigurationPropertiesBinder.register(beanFactory);
		DefaultBootstrapWriterContext writerContext = new DefaultBootstrapWriterContext("com.example", "Test");
		assertThat(writeBeanRegistration(BEAN_NAME, beanFactory.getMergedBeanDefinition(BEAN_NAME), writerContext)).lines()
				.contains("org.springframework.boot.context.properties.Test.registerConfigurationPropertiesBinder(beanFactory);");
		assertBeanRegistration(writerContext.getBootstrapClass(getClass().getPackageName())).lines().containsSequence(
				"  public static void registerConfigurationPropertiesBinder(DefaultListableBeanFactory beanFactory) {",
				"    ConfigurationPropertiesBinder.register(beanFactory);",
				"  }");
	}

	private CodeSnippet writeBeanRegistration(String beanName, BeanDefinition beanDefinition,
			BootstrapWriterContext writerContext) {
		return CodeSnippet.of((code) -> this.supplier.get(beanName, beanDefinition)
				.writeBeanRegistration(writerContext, code));
	}

	private TextAssert assertBeanRegistration(BootstrapClass bootstrapClass) {
		try {
			StringWriter out = new StringWriter();
			bootstrapClass.toJavaFile().writeTo(out);
			return new TextAssert(out.toString());
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

}
