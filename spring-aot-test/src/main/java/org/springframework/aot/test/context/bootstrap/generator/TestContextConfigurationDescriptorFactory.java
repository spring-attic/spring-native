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

package org.springframework.aot.test.context.bootstrap.generator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.test.context.BootstrapContext;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.CacheAwareContextLoaderDelegate;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContextBootstrapper;
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate;
import org.springframework.test.context.support.DefaultBootstrapContext;
import org.springframework.test.context.support.DefaultTestContextBootstrapper;

/**
 * Creates a {@link TestContextConfigurationDescriptor} with a configurable strategy to
 * identify a matching {@link AotTestContextProcessor}.
 *
 * @author Stephane Nicoll
 * @see AotTestContextProcessor
 */
class TestContextConfigurationDescriptorFactory {

	private final List<AotTestContextProcessor> aotTestContextProcessors;

	private final CacheAwareContextLoaderDelegate contextLoaderDelegate;

	TestContextConfigurationDescriptorFactory(List<AotTestContextProcessor> aotTestContextProcessors) {
		this.aotTestContextProcessors = aotTestContextProcessors;
		this.contextLoaderDelegate = new DefaultCacheAwareContextLoaderDelegate();
	}

	TestContextConfigurationDescriptorFactory(ClassLoader classLoader) {
		this(SpringFactoriesLoader.loadFactories(AotTestContextProcessor.class, classLoader));
	}

	List<TestContextConfigurationDescriptor> buildConfigurationDescriptors(Iterable<Class<?>> testClasses) {
		List<TestContextConfigurationDescriptor> descriptors = new ArrayList<>();
		for (Class<?> testClass : testClasses) {
			TestContextBootstrapper testContextBootstrapper = createTestContextBootstrapper(testClass);
			MergedContextConfiguration contextConfiguration = testContextBootstrapper.buildMergedContextConfiguration();
			AotTestContextProcessor testContextProcessor = findAotTestContextProcessor(testContextBootstrapper);
			TestContextConfigurationDescriptor existingDescriptor = descriptors.stream()
					.filter((descriptor) -> descriptor.isSameContext(contextConfiguration))
					.findFirst().orElse(null);
			if (existingDescriptor != null) {
				existingDescriptor.registerTestClass(testClass);
			}
			else {
				descriptors.add(new TestContextConfigurationDescriptor(testContextBootstrapper.getClass(),
						contextConfiguration, testContextProcessor));
			}
		}
		return descriptors;
	}

	private AotTestContextProcessor findAotTestContextProcessor(TestContextBootstrapper bootstrapper) {
		for (AotTestContextProcessor processor : this.aotTestContextProcessors) {
			if (processor.supports(bootstrapper)) {
				return processor;
			}
		}
		throw new IllegalStateException("No processor found for " + bootstrapper);
	}

	/**
	 * Build the {@link MergedContextConfiguration} of the specified {@code testClass}
	 * @param testClass the test class to handle
	 * @return a merged context configuration for the specified test class
	 */
	TestContextBootstrapper createTestContextBootstrapper(Class<?> testClass) {
		BootstrapContext bootstrapContext = new DefaultBootstrapContext(testClass, this.contextLoaderDelegate);
		TestContextBootstrapper bootstrapper = BeanUtils.instantiateClass(getTestContextBootstrapperType(testClass));
		bootstrapper.setBootstrapContext(bootstrapContext);
		return bootstrapper;
	}

	@SuppressWarnings("unchecked")
	private Class<? extends TestContextBootstrapper> getTestContextBootstrapperType(Class<?> testClass) {
		MergedAnnotations annotations = MergedAnnotations.from(testClass, SearchStrategy.INHERITED_ANNOTATIONS);
		MergedAnnotation<BootstrapWith> annotation = annotations.get(BootstrapWith.class);
		return (annotation.isPresent())
				? (Class<? extends TestContextBootstrapper>) annotation.getClass("value")
				: DefaultTestContextBootstrapper.class;
	}

}
