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

package org.springframework.boot;

import org.springframework.aot.context.bootstrap.generator.BeanDefinitionExcludeFilter;
import org.springframework.beans.factory.config.BeanDefinition;

/**
 * A {@link BeanDefinitionExcludeFilter} for Spring Boot's context infrastructure.
 *
 * @author Stephane Nicoll
 */
class BootInfrastructureExcludeFilter implements BeanDefinitionExcludeFilter {

	private final BeanDefinitionExcludeFilter filter = BeanDefinitionExcludeFilter
			.forBeanNames("org.springframework.boot.autoconfigure.internalCachingMetadataReaderFactory");

	@Override
	public boolean isExcluded(String beanName, BeanDefinition beanDefinition) {
		return this.filter.isExcluded(beanName, beanDefinition);
	}

}
