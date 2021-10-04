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

package org.springframework.boot.web.server;

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.origin.BeanFactoryStructureAnalysis;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebApplicationContext;
import org.springframework.context.annotation.BuildTimeBeanDefinitionsRegistrar;
import org.springframework.context.support.GenericApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link WebServerFactoryAutoConfigurationBeanDefinitionOriginAnalyzer}.
 * @author Stephane Nicoll
 */
class WebServerFactoryAutoConfigurationBeanDefinitionOriginAnalyzerTests {

	private final WebServerFactoryAutoConfigurationBeanDefinitionOriginAnalyzer analyzer = new WebServerFactoryAutoConfigurationBeanDefinitionOriginAnalyzer();

	@Test
	void webServerFactoryCustomizerBeanPostProcessorIsAnalyzed() {
		GenericApplicationContext context = new AnnotationConfigServletWebApplicationContext();
		context.registerBean(ServletWebServerFactoryAutoConfiguration.class);
		BuildTimeBeanDefinitionsRegistrar registrar = new BuildTimeBeanDefinitionsRegistrar();
		BeanFactoryStructureAnalysis analysis = BeanFactoryStructureAnalysis.of(registrar.processBeanDefinitions(context));
		this.analyzer.analyze(analysis);
		assertThat(analysis.resolved().filter((candidate) ->
				WebServerFactoryCustomizerBeanPostProcessor.class.getName().equals(candidate.getBeanDefinition().getBeanClassName())))
				.singleElement().satisfies((origin) -> assertThat(origin.getOrigins()).singleElement().satisfies((parent) -> {
			assertThat(context.containsBean(parent)).isTrue();
			assertThat(context.getBeanDefinition(parent).getBeanClassName()).isEqualTo(ServletWebServerFactoryAutoConfiguration.class.getName());
		}));
	}

	@Test
	void errorPageRegistrarBeanPostProcessorIsAnalyzed() {
		GenericApplicationContext context = new AnnotationConfigServletWebApplicationContext();
		context.registerBean(ServletWebServerFactoryAutoConfiguration.class);
		BuildTimeBeanDefinitionsRegistrar registrar = new BuildTimeBeanDefinitionsRegistrar();
		BeanFactoryStructureAnalysis analysis = BeanFactoryStructureAnalysis.of(registrar.processBeanDefinitions(context));
		this.analyzer.analyze(analysis);
		assertThat(analysis.resolved().filter((candidate) ->
				ErrorPageRegistrarBeanPostProcessor.class.getName().equals(candidate.getBeanDefinition().getBeanClassName())))
				.singleElement().satisfies((origin) -> assertThat(origin.getOrigins()).singleElement().satisfies((parent) -> {
			assertThat(context.containsBean(parent)).isTrue();
			assertThat(context.getBeanDefinition(parent).getBeanClassName()).isEqualTo(ServletWebServerFactoryAutoConfiguration.class.getName());
		}));
	}

}
