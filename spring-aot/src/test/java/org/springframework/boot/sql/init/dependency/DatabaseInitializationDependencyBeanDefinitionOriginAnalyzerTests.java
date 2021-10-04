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

package org.springframework.boot.sql.init.dependency;

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.origin.BeanFactoryStructureAnalysis;
import org.springframework.boot.sql.init.dependency.DatabaseInitializationDependencyConfigurer.DependsOnDatabaseInitializationPostProcessor;
import org.springframework.context.annotation.BuildTimeBeanDefinitionsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DatabaseInitializationDependencyBeanDefinitionOriginAnalyzer}.
 *
 * @author Stephane Nicoll
 */
class DatabaseInitializationDependencyBeanDefinitionOriginAnalyzerTests {

	private final DatabaseInitializationDependencyBeanDefinitionOriginAnalyzer analyzer = new DatabaseInitializationDependencyBeanDefinitionOriginAnalyzer();

	@Test
	void analyzeDependsOnDatabaseInitializationPostProcessor() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(SampleConfiguration.class);
		BeanFactoryStructureAnalysis analysis = BeanFactoryStructureAnalysis.of(
				new BuildTimeBeanDefinitionsRegistrar().processBeanDefinitions(context));
		this.analyzer.analyze(analysis);
		assertThat(analysis.resolved()).singleElement().satisfies((beanDefinition) -> {
			assertThat(beanDefinition.getBeanDefinition().getResolvableType().toClass())
					.isEqualTo(DependsOnDatabaseInitializationPostProcessor.class);
			assertThat(beanDefinition.getOrigins()).singleElement().satisfies((parent) -> {
				assertThat(context.containsBean(parent)).isTrue();
				assertThat(context.getBeanDefinition(parent).getResolvableType().toClass())
						.isEqualTo(SampleConfiguration.class);
			});
		});

	}

	@Configuration(proxyBeanMethods = false)
	@Import(DatabaseInitializationDependencyConfigurer.class)
	static class SampleConfiguration {

	}
}
