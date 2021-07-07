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

package org.springframework.context.origin;

import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.BuildTimeBeanDefinitionsRegistrar;
import org.springframework.context.annotation.samples.simple.ConfigurationOne;
import org.springframework.context.support.GenericApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BeanFactoryStructureAnalyzer}.
 *
 * @author Stephane Nicoll
 */
class BeanFactoryStructureAnalyzerTests {

	@Test
	void analyzeSimpleStructure() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(ConfigurationOne.class);
		BeanFactoryStructure structure = analyze(context);
		assertThat(structure).isNotNull();
	}

	private BeanFactoryStructure analyze(GenericApplicationContext context) {
		return new BeanFactoryStructureAnalyzer(context.getClassLoader()).analyze(
				new BuildTimeBeanDefinitionsRegistrar(context).processBeanDefinitions());
	}

}
