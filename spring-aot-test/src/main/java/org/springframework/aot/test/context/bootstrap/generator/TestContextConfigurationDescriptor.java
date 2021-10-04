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
import java.util.function.Supplier;

import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.MergedContextConfiguration;

/**
 * Describe a particular test configuration, alongside the test classes that matches.
 *
 * @author Stephane Nicoll
 */
class TestContextConfigurationDescriptor {

	private final MergedContextConfiguration contextConfiguration;

	private final Supplier<GenericApplicationContext> contextSupplier;

	private final List<Class<?>> testClasses;

	TestContextConfigurationDescriptor(MergedContextConfiguration configuration, Supplier<GenericApplicationContext> contextSupplier) {
		this.contextConfiguration = configuration;
		this.contextSupplier = contextSupplier;
		this.testClasses = new ArrayList<>();
		this.testClasses.add(configuration.getTestClass());
	}

	/**
	 * Parse the {@link GenericApplicationContext context} of this instance.
	 * @return a parsed context (not refreshed)
	 */
	GenericApplicationContext parseTestContext() {
		return this.contextSupplier.get();
	}

	/**
	 * Specify if the {@link MergedContextConfiguration} represents the same context as
	 * the one handled by this instance
	 * @param config the config to check
	 * @return {@code true} if it represents the same context, {@code false} otherwise
	 */
	boolean isSameContext(MergedContextConfiguration config) {
		return this.contextConfiguration.equals(config);
	}

	/**
	 * Register an additional test class to this instance
	 * @param testClass the additional test class to register
	 */
	void registerTestClass(Class<?> testClass) {
		this.testClasses.add(testClass);
	}

	/**
	 * Return the test classes that this descriptor manages.
	 * @return the registered test classes for this test context
	 */
	List<Class<?>> getTestClasses() {
		return this.testClasses;
	}

}
