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

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import org.springframework.aot.test.samples.app.SampleApplicationAnotherTests;
import org.springframework.aot.test.samples.app.SampleApplicationTests;
import org.springframework.aot.test.samples.app.slice.SampleJdbcTests;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate;
import org.springframework.test.context.support.DefaultBootstrapContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link TestContextConfigurationDescriptor}.
 *
 * @author Stephane Nicoll
 */
class TestContextConfigurationDescriptorTests {

	@Test
	@SuppressWarnings("unchecked")
	void parseTestContextUseSupplier() {
		Supplier<GenericApplicationContext> supplier = mock(Supplier.class);
		GenericApplicationContext context = new GenericApplicationContext();
		given(supplier.get()).willReturn(context);
		TestContextConfigurationDescriptor descriptor = new TestContextConfigurationDescriptor(
				createMergedContextConfiguration(SampleApplicationTests.class), supplier);
		assertThat(descriptor.parseTestContext()).isSameAs(context);
		verify(supplier).get();
	}

	@Test
	void isSameWithEquivalentMergedContextConfiguration() {
		TestContextConfigurationDescriptor descriptor = new TestContextConfigurationDescriptor(
				createMergedContextConfiguration(SampleApplicationTests.class), GenericApplicationContext::new);
		assertThat(descriptor.isSameContext(createMergedContextConfiguration(SampleApplicationAnotherTests.class))).isTrue();
	}

	@Test
	void isSameWithDifferentMergedContextConfiguration() {
		TestContextConfigurationDescriptor descriptor = new TestContextConfigurationDescriptor(
				createMergedContextConfiguration(SampleApplicationTests.class), GenericApplicationContext::new);
		assertThat(descriptor.isSameContext(createMergedContextConfiguration(SampleJdbcTests.class))).isFalse();
	}

	@Test
	void getTestClassesContainOriginalTestClass() {
		TestContextConfigurationDescriptor descriptor = new TestContextConfigurationDescriptor(
				createMergedContextConfiguration(SampleApplicationTests.class), GenericApplicationContext::new);
		assertThat(descriptor.getTestClasses()).containsOnly(SampleApplicationTests.class);
	}

	@Test
	void registerTestClassAppendToList() {
		TestContextConfigurationDescriptor descriptor = new TestContextConfigurationDescriptor(
				createMergedContextConfiguration(SampleApplicationTests.class), GenericApplicationContext::new);
		descriptor.registerTestClass(SampleApplicationAnotherTests.class);
		assertThat(descriptor.getTestClasses()).containsOnly(SampleApplicationTests.class, SampleApplicationAnotherTests.class);
	}


	private MergedContextConfiguration createMergedContextConfiguration(Class<?> testClass) {
		SpringBootTestContextBootstrapper bootstrapper = new SpringBootTestContextBootstrapper();
		bootstrapper.setBootstrapContext(new DefaultBootstrapContext(testClass, new DefaultCacheAwareContextLoaderDelegate()));
		return bootstrapper.buildMergedContextConfiguration();
	}

}
