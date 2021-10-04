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

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.bean.BeanRegistrationWriter;
import org.springframework.aot.context.bootstrap.generator.infrastructure.BootstrapClass;
import org.springframework.aot.context.bootstrap.generator.infrastructure.BootstrapWriterContext;
import org.springframework.aot.context.bootstrap.generator.sample.context.properties.JavaBeanSampleBean;
import org.springframework.aot.context.bootstrap.generator.sample.context.properties.ValueObjectSampleBean;
import org.springframework.aot.context.bootstrap.generator.test.CodeSnippet;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.context.properties.ConfigurationPropertiesBean.BindMethod;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ConfigurationPropertiesBeanRegistrationWriterSupplier}.
 *
 * @author Stephane Nicoll
 */
class ConfigurationPropertiesBeanRegistrationWriterSupplierTests {

	private final ConfigurationPropertiesBeanRegistrationWriterSupplier supplier = new ConfigurationPropertiesBeanRegistrationWriterSupplier();

	@Test
	void writeBeanRegistrationForValueObjectConfigurationProperties() {
		RootBeanDefinition rootBeanDefinition = (RootBeanDefinition) BeanDefinitionBuilder
				.rootBeanDefinition(ValueObjectSampleBean.class).getBeanDefinition();
		rootBeanDefinition.setAttribute(BindMethod.class.getName(), BindMethod.VALUE_OBJECT);
		BeanRegistrationWriter writer = supplier.get("test", rootBeanDefinition);
		assertThat(CodeSnippet.of((code) -> writer.writeBeanRegistration(createBootstrapContext(), code))).lines().contains(
				"BeanDefinitionRegistrar.of(\"test\", ValueObjectSampleBean.class)",
				"    .instanceSupplier(() -> ConstructorBindingValueSupplier.bind(context.getBeanFactory(), \"test\", ValueObjectSampleBean.class))"
						+ ".customize((bd) -> bd.setAttribute(\"org.springframework.boot.context.properties.ConfigurationPropertiesBean$BindMethod\", ConfigurationPropertiesBean.BindMethod.VALUE_OBJECT))"
						+ ".register(context);");
	}

	@Test
	void writeBeanRegistrationForJavaBeanConfigurationProperties() {
		RootBeanDefinition rootBeanDefinition = (RootBeanDefinition) BeanDefinitionBuilder
				.rootBeanDefinition(JavaBeanSampleBean.class).getBeanDefinition();
		rootBeanDefinition.setAttribute(BindMethod.class.getName(), BindMethod.JAVA_BEAN);
		assertThat(supplier.get("test", rootBeanDefinition)).isNull();
	}

	@Test
	void writeBeanRegistrationForNonConfigurationPropertiesType() {
		RootBeanDefinition rootBeanDefinition = (RootBeanDefinition) BeanDefinitionBuilder
				.rootBeanDefinition(String.class).getBeanDefinition();
		assertThat(supplier.get("test", rootBeanDefinition)).isNull();
	}

	private static BootstrapWriterContext createBootstrapContext() {
		return new BootstrapWriterContext(BootstrapClass.of("com.example"));
	}

}
