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

package org.springframework.cloud;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;
import org.junit.jupiter.api.Test;

import org.springframework.aot.beans.factory.config.NoOpScope;
import org.springframework.aot.context.bootstrap.generator.infrastructure.DefaultBootstrapWriterContext;
import org.springframework.aot.context.bootstrap.generator.test.CodeSnippet;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SpringCloudRefreshScopeHandler}.
 *
 * @author Stephane Nicoll
 */
class SpringCloudRefreshScopeHandlerTests {

	@Test
	void beanFactoryWithoutScopeDoesNotAddNoOpScope() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("test", BeanDefinitionBuilder.rootBeanDefinition(TestComponent.class).getBeanDefinition());
		Builder builder = CodeBlock.builder();
		process(beanFactory, builder);
		assertThat(builder.isEmpty()).isTrue();
	}

	@Test
	void beanFactoryWithRefreshScopeAddsNoOpScope() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("configuration", BeanDefinitionBuilder.rootBeanDefinition(TestConfiguration.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("test", BeanDefinitionBuilder.rootBeanDefinition(TestComponent.class)
				.setFactoryMethodOnBean("refreshTestComponent", "configuration").getBeanDefinition());
		Builder builder = CodeBlock.builder();
		process(beanFactory, builder);
		assertThat(CodeSnippet.of(builder.build())).hasImport(NoOpScope.class)
				.contains("beanFactory.registerScope(\"refresh\", new NoOpScope());");
	}


	private void process(DefaultListableBeanFactory beanFactory, CodeBlock.Builder code) {
		DefaultBootstrapWriterContext writerContext = new DefaultBootstrapWriterContext("com.example", "Test");
		new SpringCloudRefreshScopeHandler(beanFactory).writeNoOpRefreshScope(writerContext, code);
	}


	@Component
	private static class TestComponent {

	}

	static class TestConfiguration {

		@RefreshScope
		TestComponent refreshTestComponent() {
			return new TestComponent();
		}

	}

}
