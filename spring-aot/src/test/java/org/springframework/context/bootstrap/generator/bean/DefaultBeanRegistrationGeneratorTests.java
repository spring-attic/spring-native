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

package org.springframework.context.bootstrap.generator.bean;

import java.util.function.Consumer;

import com.squareup.javapoet.CodeBlock.Builder;
import org.junit.jupiter.api.Test;

import org.springframework.aot.beans.factory.BeanDefinitionRegistrar;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.sample.factory.SampleFactory;
import org.springframework.context.bootstrap.generator.test.CodeSnippet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DefaultBeanRegistrationGenerator}.
 *
 * @author Stephane Nicoll
 */
class DefaultBeanRegistrationGeneratorTests {

	@Test
	void writeWithSyntheticFlag() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder
				.rootBeanDefinition(SampleFactory.class.getName(), "create").setSynthetic(true).getBeanDefinition();
		assertThat(generateCode(beanDefinition, (code) -> code.add("() -> SampleFactory::new"))).lines()
				.containsOnly("BeanDefinitionRegistrar.of(\"test\", Object.class).instanceSupplier(() -> SampleFactory::new)"
						+ ".customize((builder) -> builder.setSynthetic(true)).register(context);");
	}

	private CodeSnippet generateCode(BeanDefinition beanDefinition, Consumer<Builder> instanceSupplier) {
		return CodeSnippet.of((code) -> {
			SimpleBeanValueWriter beanValueWriter = new SimpleBeanValueWriter(new BeanInstanceDescriptor(
					beanDefinition.getResolvableType().toClass(), null), instanceSupplier);
			new DefaultBeanRegistrationGenerator("test", beanDefinition, beanValueWriter).writeBeanRegistration(code);
		});
	}

}
