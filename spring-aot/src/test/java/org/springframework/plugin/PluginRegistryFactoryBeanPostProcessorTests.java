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

package org.springframework.plugin;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.BeanDefinitionPostProcessor;
import org.springframework.context.bootstrap.generator.sample.factory.NumberHolderFactoryBean;
import org.springframework.core.ResolvableType;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.plugin.core.support.PluginRegistryFactoryBean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PluginRegistryFactoryBeanPostProcessor}.
 *
 * @author Stephane Nicoll
 */
class PluginRegistryFactoryBeanPostProcessorTests {

	private final BeanDefinitionPostProcessor postProcessor = new PluginRegistryFactoryBeanPostProcessor();

	@Test
	void postProcessPluginRegistryFactoryBean() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(PluginRegistryFactoryBean.class, EntityLinks.class, Class.class));
		postProcessor.postProcessBeanDefinition("test", beanDefinition);
		assertThat(beanDefinition.hasBeanClass()).isTrue();
		assertThat(beanDefinition.getBeanClass()).isEqualTo(PluginRegistryFactoryBean.class);
		assertThat(beanDefinition.getResolvableType()).satisfies((resolvableType) -> {
			assertThat(resolvableType.toClass()).isEqualTo(PluginRegistry.class);
			assertThat(resolvableType.getGenerics()).hasSize(2);
			assertThat(resolvableType.getGenerics()[0].toClass()).isEqualTo(EntityLinks.class);
			assertThat(resolvableType.getGenerics()[1].toClass()).isEqualTo(Class.class);
		});
	}

	@Test
	void postProcessUnrelatedFactoryBean() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setTargetType(NumberHolderFactoryBean.class);
		postProcessor.postProcessBeanDefinition("test", beanDefinition);
		assertThat(beanDefinition.hasBeanClass()).isFalse();
		assertThat(beanDefinition.getResolvableType().toClass()).isEqualTo(NumberHolderFactoryBean.class);
	}

	@Test
	void postProcessRegularType() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(Function.class, Integer.class, String.class));
		postProcessor.postProcessBeanDefinition("test", beanDefinition);
		assertThat(beanDefinition.hasBeanClass()).isFalse();
		assertThat(beanDefinition.getResolvableType().toClass()).isEqualTo(Function.class);
	}

}
