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

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.origin.BeanDefinitionDescriptor;
import org.springframework.aot.context.origin.BeanDefinitionDescriptor.Type;
import org.springframework.aot.context.origin.BeanFactoryStructureAnalysis;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.context.annotation.BuildTimeBeanDefinitionsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ConfigurationPropertiesBeanDefinitionOriginAnalyzer}.
 *
 * @author Stephane Nicoll
 */
class ConfigurationPropertiesBeanDefinitionOriginAnalyzerTests {

	private final ConfigurationPropertiesBeanDefinitionOriginAnalyzer analyzer = new ConfigurationPropertiesBeanDefinitionOriginAnalyzer();

	@Test
	void analyzeConfigurationPropertiesAnnotatedType() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("configuration", new RootBeanDefinition(SampleConfiguration.class));
		beanFactory.registerBeanDefinition("configurationProperties", new RootBeanDefinition(SampleProperties.class));
		BeanFactoryStructureAnalysis analysis = BeanFactoryStructureAnalysis.of(beanFactory);
		this.analyzer.analyze(analysis);
		assertThat(analysis.resolved()).singleElement().satisfies((processed) -> {
			assertThat(processed.getBeanDefinition()).isEqualTo(beanFactory.getBeanDefinition("configurationProperties"));
			assertThat(processed.getType()).isEqualTo(Type.COMPONENT);
			assertThat(processed.getOrigins()).singleElement().satisfies((parent) -> {
				assertThat(beanFactory.containsBean(parent)).isTrue();
				assertThat(beanFactory.getBeanDefinition(parent).getBeanClassName()).isEqualTo(SampleConfiguration.class.getName());
			});
		});
	}

	@Test
	void analyzeConfigurationPropertiesInfrastructure() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(SampleAutoConfiguration.class);
		BuildTimeBeanDefinitionsRegistrar registrar = new BuildTimeBeanDefinitionsRegistrar();
		BeanFactoryStructureAnalysis analysis = BeanFactoryStructureAnalysis.of(registrar.processBeanDefinitions(context));
		this.analyzer.analyze(analysis);
		assertThat(analysis.resolved()).hasSize(5);
		HashSet<String> parents = analysis.resolved().map(BeanDefinitionDescriptor::getOrigins)
				.collect(HashSet::new, Set::addAll, Set::addAll);
		assertThat(parents).singleElement().satisfies((parent) -> assertThat(parent)
				.isEqualTo(ConfigurationPropertiesAutoConfiguration.class.getName()));
	}


	@Configuration(proxyBeanMethods = false)
	@EnableConfigurationProperties(SampleProperties.class)
	static class SampleConfiguration {

	}

	@ConfigurationProperties("sample")
	static class SampleProperties {

	}

	@Configuration(proxyBeanMethods = false)
	@ImportAutoConfiguration(ConfigurationPropertiesAutoConfiguration.class)
	static class SampleAutoConfiguration {

	}

}
