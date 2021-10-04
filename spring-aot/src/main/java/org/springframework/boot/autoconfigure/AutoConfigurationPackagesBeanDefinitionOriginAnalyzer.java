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

package org.springframework.boot.autoconfigure;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.aot.context.origin.BeanDefinitionDescriptor;
import org.springframework.aot.context.origin.BeanDefinitionDescriptor.Type;
import org.springframework.aot.context.origin.BeanDefinitionDescriptorPredicates;
import org.springframework.aot.context.origin.BeanDefinitionOriginAnalyzer;
import org.springframework.aot.context.origin.BeanFactoryStructureAnalysis;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages.BasePackages;

/**
 * A {@link BeanDefinitionOriginAnalyzer} for Spring Boot's auto-configuration packages
 * support.
 *
 * @author Stephane Nicoll
 */
class AutoConfigurationPackagesBeanDefinitionOriginAnalyzer implements BeanDefinitionOriginAnalyzer {

	@Override
	public void analyze(BeanFactoryStructureAnalysis analysis) {
		BeanDefinitionDescriptorPredicates predicates = analysis.getPredicates();
		BeanDefinitionDescriptor descriptor = analysis.unresolved().filter(predicates.ofBeanClassName(BasePackages.class))
				.findAny().orElse(null);
		if (descriptor != null) {
			Set<String> origins = analysis.beanDefinitions().filter(predicates.annotatedWith(AutoConfigurationPackage.class))
					.map(BeanDefinitionDescriptor::getBeanName).collect(Collectors.toSet());
			analysis.markAsResolved(descriptor.resolve(Type.INFRASTRUCTURE, origins));
		}
	}

}
