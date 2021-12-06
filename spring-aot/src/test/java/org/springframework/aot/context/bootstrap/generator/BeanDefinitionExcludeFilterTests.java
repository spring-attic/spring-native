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

package org.springframework.aot.context.bootstrap.generator;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.support.RootBeanDefinition;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BeanDefinitionExcludeFilter}.
 *
 * @author Stephane Nicoll
 */
class BeanDefinitionExcludeFilterTests {

	@Test
	void forBeanNamesExcludeMatchingName() {
		assertThat(BeanDefinitionExcludeFilter.forBeanNames("test").isExcluded("test",
				new RootBeanDefinition())).isTrue();
	}

	@Test
	void forBeanNamesDoesNotExcludeNonMatchingName() {
		assertThat(BeanDefinitionExcludeFilter.forBeanNames("test").isExcluded("another",
				new RootBeanDefinition())).isFalse();
	}

	@Test
	void forBeanTypesExcludeExactMatch() {
		assertThat(BeanDefinitionExcludeFilter.forTypes(Integer.class).isExcluded("test",
				new RootBeanDefinition(Integer.class))).isTrue();
	}

	@Test
	void forBeanTypesExcludeExactAssignableType() {
		assertThat(BeanDefinitionExcludeFilter.forTypes(Number.class).isExcluded("test",
				new RootBeanDefinition(Integer.class))).isTrue();
	}

	@Test
	void forBeanTypesDoesNotExcludeNonAssignableType() {
		assertThat(BeanDefinitionExcludeFilter.forTypes(Integer.class).isExcluded("test",
				new RootBeanDefinition(Number.class))).isFalse();
	}

}
