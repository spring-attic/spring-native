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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.aot.context.origin.BeanDefinitionDescriptor;
import org.springframework.aot.context.origin.BeanDefinitionDescriptor.Type;
import org.springframework.aot.context.origin.BeanDefinitionDescriptorPredicates;
import org.springframework.aot.context.origin.BeanDefinitionOriginAnalyzer;
import org.springframework.aot.context.origin.BeanFactoryStructureAnalysis;
import org.springframework.boot.sql.init.dependency.DatabaseInitializationDependencyConfigurer.DependsOnDatabaseInitializationPostProcessor;
import org.springframework.context.annotation.Import;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.MultiValueMap;

/**
 * A {@link BeanDefinitionOriginAnalyzer} for Spring Boot's database initialization
 * support.
 *
 * @author Stephane Nicoll
 */
class DatabaseInitializationDependencyBeanDefinitionOriginAnalyzer implements BeanDefinitionOriginAnalyzer {

	@Override
	public void analyze(BeanFactoryStructureAnalysis analysis) {
		BeanDefinitionDescriptorPredicates predicates = analysis.getPredicates();
		analysis.unresolved().filter(predicates.ofBeanClassName(DependsOnDatabaseInitializationPostProcessor.class))
				.findAny().ifPresent((candidate) -> {
			Set<String> origins = analysis.beanDefinitions()
					.filter(predicates.annotationMatching(this::hasImport)).map(BeanDefinitionDescriptor::getBeanName).collect(Collectors.toSet());
			analysis.markAsResolved(candidate.resolve(Type.INFRASTRUCTURE, origins));
		});
	}

	@SuppressWarnings("unchecked")
	private boolean hasImport(AnnotationMetadata metadata) {
		if (!metadata.isAnnotated(Import.class.getName())) {
			return false;
		}
		MultiValueMap<String, Object> attributes = metadata.getAllAnnotationAttributes(Import.class.getName(), true);
		Object values = (attributes != null ? attributes.get("value") : null);
		if (values != null) {
			List<String[]> arrayValues = (List<String[]>) values;
			for (String[] arrayValue : arrayValues) {
				if (Arrays.asList(arrayValue).contains(DatabaseInitializationDependencyConfigurer.class.getName())) {
					return true;
				}
			}
		}
		return false;
	}
}
