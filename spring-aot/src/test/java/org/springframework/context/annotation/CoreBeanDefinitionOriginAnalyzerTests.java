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

package org.springframework.context.annotation;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.origin.BeanDefinitionDescriptor;
import org.springframework.aot.context.origin.BeanFactoryStructureAnalysis;
import org.springframework.context.annotation.samples.compose.ImportConfiguration;
import org.springframework.context.annotation.samples.simple.ConfigurationOne;
import org.springframework.context.annotation.samples.simple.ConfigurationTwo;
import org.springframework.context.support.GenericApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CoreBeanDefinitionOriginAnalyzer}.
 *
 * @author Stephane Nicoll
 */
class CoreBeanDefinitionOriginAnalyzerTests {

	@Test
	void analyzeConfigurationClass() {
		BeanFactoryStructureAnalysis analysis = analyze(ImportConfiguration.class);
		assertThat(analysis.unresolved()).isEmpty();
		Map<String, BeanDefinitionDescriptor> resolved = index(analysis.resolved());
		assertThat(resolved).hasSize(5);
		assertThat(resolved).containsOnlyKeys(
				ImportConfiguration.class.getName(), ConfigurationOne.class.getName(), "beanOne",
				ConfigurationTwo.class.getName(), "beanTwo");
		assertThat(resolved.get(ConfigurationOne.class.getName()).getOrigins())
				.containsOnly(ImportConfiguration.class.getName());
		assertThat(resolved.get(ConfigurationTwo.class.getName()).getOrigins())
				.containsOnly(ImportConfiguration.class.getName());
		assertThat(resolved.get(ImportConfiguration.class.getName()).getOrigins()).isEmpty();
	}

	@Test
	void analyzeBeanMethod() {
		BeanFactoryStructureAnalysis analysis = analyze(ConfigurationOne.class);
		assertThat(analysis.unresolved()).isEmpty();
		Map<String, BeanDefinitionDescriptor> resolved = index(analysis.resolved());
		assertThat(resolved).hasSize(2);
		assertThat(resolved).containsOnlyKeys(ConfigurationOne.class.getName(), "beanOne");
		assertThat(resolved.get("beanOne").getOrigins())
				.containsOnly(ConfigurationOne.class.getName());
	}

	private Map<String, BeanDefinitionDescriptor> index(Stream<BeanDefinitionDescriptor> descriptors) {
		return descriptors.collect(Collectors.toMap(BeanDefinitionDescriptor::getBeanName, (descriptor) -> descriptor));
	}

	private BeanFactoryStructureAnalysis analyze(Class<?>... components) {
		GenericApplicationContext context = new GenericApplicationContext();
		for (Class<?> component : components) {
			context.registerBean(component);
		}
		BuildTimeBeanDefinitionsRegistrar registrar = new BuildTimeBeanDefinitionsRegistrar();
		BeanFactoryStructureAnalysis analysis = BeanFactoryStructureAnalysis.of(registrar.processBeanDefinitions(context));
		new CoreBeanDefinitionOriginAnalyzer().analyze(analysis);
		return analysis;
	}

}
