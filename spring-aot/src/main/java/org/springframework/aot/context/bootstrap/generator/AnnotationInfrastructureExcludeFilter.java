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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigUtils;

/**
 * A {@link BeanDefinitionExcludeFilter} for the core annotation support.
 *
 * @author Stephane Nicoll
 */
class AnnotationInfrastructureExcludeFilter implements BeanDefinitionExcludeFilter {

	private final BeanDefinitionExcludeFilter filter = BeanDefinitionExcludeFilter.forBeanNames(
			// Configuration processing is happening at build time.
			AnnotationConfigUtils.CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME,
			AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR,

			// Event listener registrations is done at build time
			AnnotationConfigUtils.EVENT_LISTENER_PROCESSOR_BEAN_NAME,
			AnnotationConfigUtils.EVENT_LISTENER_FACTORY_BEAN_NAME,

			// Injection points are processed at build time
			AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME,
			AnnotationConfigUtils.COMMON_ANNOTATION_PROCESSOR_BEAN_NAME);

	@Override
	public boolean isExcluded(String beanName, BeanDefinition beanDefinition) {
		return this.filter.isExcluded(beanName, beanDefinition);
	}

}
