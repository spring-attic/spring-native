/*
 * Copyright 2021 the original author or authors.
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
package org.springframework.nativex.substitutions.data;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.context.index.CandidateComponentsIndex;
import org.springframework.context.index.CandidateComponentsIndexLoader;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.data.repository.config.ImplementationDetectionConfiguration;
import org.springframework.nativex.substitutions.OnlyIfPresent;
import org.springframework.stereotype.Component;


@TargetClass(className = "org.springframework.data.repository.config.CustomRepositoryImplementationDetector", onlyWith = {OnlyIfPresent.class})
public final class Target_CustomRepositoryImplementationDetector {

	@Substitute
	private Set<BeanDefinition> findCandidateBeanDefinitions(ImplementationDetectionConfiguration config) {

		/* Using the index instead of ClassPathScanningCandidateComponentProvider with pattern.
		 * Not sure why components are not found via the index as it should be configured on
		 * `setResourceLoader` within ClassPathScanningCandidateComponentProvider.
		 *
		 * ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false, environment);
		 *
		 * provider.setResourceLoader(resourceLoader);
		 * provider.setResourcePattern(String.format(CUSTOM_IMPLEMENTATION_RESOURCE_PATTERN, postfix));
		 * provider.setMetadataReaderFactory(config.getMetadataReaderFactory());
		 * provider.addIncludeFilter((reader, factory) -> true);
		 */
		CandidateComponentsIndex index = CandidateComponentsIndexLoader.loadIndex(config.getClass().getClassLoader());

		return config.getBasePackages().stream()

				.flatMap(new Function<String, Stream<? extends BeanDefinition>>() { // see oracle/graal#2479

							 @Override
							 public Stream<? extends BeanDefinition> apply(String basePackage) {

								 Set<String> candidateTypes = index.getCandidateTypes(basePackage, Component.class.getName());
								 if (candidateTypes.isEmpty()) {
									 return Stream.empty();
								 }

								 Set<BeanDefinition> beanDefinitions = new LinkedHashSet<>();
								 for (String candidate : candidateTypes) {
									 if (candidate.endsWith(config.getImplementationPostfix())) {

										 try {

											 MetadataReader metadataReader = config.getMetadataReaderFactory().getMetadataReader(candidate);
											 ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
											 sbd.setResource(metadataReader.getResource());
											 beanDefinitions.add(sbd);
										 } catch (IOException ex) {
											 throw new BeanDefinitionStoreException(String.format("Failure while reading metadata for %s.", candidate), ex);
										 }
									 }
								 }

								 return beanDefinitions.stream();
							 }
						 }
				).collect(Collectors.toSet());
	}
}
