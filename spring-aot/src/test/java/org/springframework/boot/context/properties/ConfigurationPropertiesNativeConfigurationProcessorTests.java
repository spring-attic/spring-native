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

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.nativex.hint.Flag;

/**
 * Tests for {@link ConfigurationPropertiesNativeConfigurationProcessor}.
 *
 * @author Stephane Nicoll
 * @author Christoph Strobl
 */
class ConfigurationPropertiesNativeConfigurationProcessorTests {

	@Test
	void processConfigurationProperties() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("beanA", BeanDefinitionBuilder.rootBeanDefinition(SampleProperties.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("beanB", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		assertThat(registry.reflection().getEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(SampleProperties.class);
			assertThat(entry.getFlags()).containsOnly(Flag.allDeclaredMethods);
		});
	}

	@Test // GH-1041
	void processConfigurationPropertiesWithMemberTypes() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("beanA", BeanDefinitionBuilder.rootBeanDefinition(SampleWithNested.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("beanB", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		assertThat(registry.reflection().getEntries()).hasSize(2).satisfies(entries -> {
			assertThat(entries.stream().map(it -> it.getType().getSimpleName() + ":" + it.getFlags())).containsExactlyInAnyOrder("SampleWithNested:[allDeclaredMethods]", "OneLevelDown:[allDeclaredMethods]");
		});
	}

	private NativeConfigurationRegistry process(DefaultListableBeanFactory beanFactory) {
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new ConfigurationPropertiesNativeConfigurationProcessor().process(beanFactory, registry);
		return registry;
	}

	@ConfigurationProperties("test")
	static class SampleProperties {

	}

	@ConfigurationProperties("with-nested")
	static class SampleWithNested {

		static class OneLevelDown {

		}
	}

}
