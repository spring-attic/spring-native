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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.test.samples.app.SampleApplicationAnotherTests;
import org.springframework.aot.test.samples.app.SampleApplicationTests;
import org.springframework.aot.test.samples.app.slice.SampleJdbcTests;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate;
import org.springframework.test.context.support.DefaultBootstrapContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
	void parseTestContextUseAotTestContextProcessor() {
		MergedContextConfiguration mergedContextConfiguration = createMergedContextConfiguration(SampleApplicationTests.class);
		GenericApplicationContext context = new GenericApplicationContext();
		AotTestContextProcessor aotTestContextProcessor = mock(AotTestContextProcessor.class);
		given(aotTestContextProcessor.prepareTestContext(mergedContextConfiguration)).willReturn(context);
		TestContextConfigurationDescriptor descriptor = new TestContextConfigurationDescriptor(SpringBootTestContextBootstrapper.class,
				mergedContextConfiguration, aotTestContextProcessor);
		assertThat(descriptor.parseTestContext()).isSameAs(context);
		verify(aotTestContextProcessor).prepareTestContext(mergedContextConfiguration);
	}

	@Test
	void contributeNativeConfigurationRegisterTestContextBootstrapper() {
		MergedContextConfiguration mergedContextConfiguration = createMergedContextConfiguration(SampleApplicationTests.class);
		TestContextConfigurationDescriptor descriptor = new TestContextConfigurationDescriptor(SpringBootTestContextBootstrapper.class,
				mergedContextConfiguration, mock(AotTestContextProcessor.class));
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		descriptor.contributeNativeConfiguration(registry);
		assertThat(registry.reflection().toClassDescriptors()).singleElement().satisfies((classDescriptor) -> {
			assertThat(classDescriptor.getName()).isEqualTo(SpringBootTestContextBootstrapper.class.getName());
			assertThat(classDescriptor.getAccess()).containsOnly(TypeAccess.DECLARED_CONSTRUCTORS);
			assertThat(classDescriptor.getFields()).isNull();
			assertThat(classDescriptor.getMethods()).isNull();
		});
	}

	@Test
	void writeTestContextLoaderInstanceSupplierUseAotTestContextProcessor() {
		MergedContextConfiguration mergedContextConfiguration = createMergedContextConfiguration(SampleApplicationTests.class);
		ClassName className = ClassName.get("com.example", "TestInit");
		AotTestContextProcessor aotTestContextProcessor = mock(AotTestContextProcessor.class);
		given(aotTestContextProcessor.writeInstanceSupplier(mergedContextConfiguration, className)).willReturn(CodeBlock.of("test"));
		TestContextConfigurationDescriptor descriptor = new TestContextConfigurationDescriptor(SpringBootTestContextBootstrapper.class,
				mergedContextConfiguration, aotTestContextProcessor);
		assertThat(descriptor.writeTestContextLoaderInstanceSupplier(className)).hasToString("test");
		verify(aotTestContextProcessor).writeInstanceSupplier(mergedContextConfiguration, className);
	}

	@Test
	void isSameWithEquivalentMergedContextConfiguration() {
		TestContextConfigurationDescriptor descriptor = createDescriptor(
				SampleApplicationTests.class, mockAotTestContextProcessor());
		assertThat(descriptor.isSameContext(createMergedContextConfiguration(SampleApplicationAnotherTests.class))).isTrue();
	}

	@Test
	void isSameWithDifferentMergedContextConfiguration() {
		TestContextConfigurationDescriptor descriptor = createDescriptor(
				SampleApplicationTests.class, mockAotTestContextProcessor());
		assertThat(descriptor.isSameContext(createMergedContextConfiguration(SampleJdbcTests.class))).isFalse();
	}

	@Test
	void getTestClassesContainOriginalTestClass() {
		TestContextConfigurationDescriptor descriptor = createDescriptor(
				SampleApplicationTests.class, mockAotTestContextProcessor());
		assertThat(descriptor.getTestClasses()).containsOnly(SampleApplicationTests.class);
	}

	@Test
	void registerTestClassAppendToList() {
		TestContextConfigurationDescriptor descriptor = createDescriptor(
				SampleApplicationTests.class, mockAotTestContextProcessor());
		descriptor.registerTestClass(SampleApplicationAnotherTests.class);
		assertThat(descriptor.getTestClasses()).containsOnly(SampleApplicationTests.class, SampleApplicationAnotherTests.class);
	}

	private TestContextConfigurationDescriptor createDescriptor(Class<?> testClass,
			AotTestContextProcessor aotTestContextProcessor) {
		return new TestContextConfigurationDescriptor(SpringBootTestContextBootstrapper.class,
				createMergedContextConfiguration(testClass), aotTestContextProcessor);
	}

	private AotTestContextProcessor mockAotTestContextProcessor() {
		AotTestContextProcessor aotTestContextProcessor = mock(AotTestContextProcessor.class);
		given(aotTestContextProcessor.prepareTestContext(any())).willReturn(new GenericApplicationContext());
		return aotTestContextProcessor;
	}

	private MergedContextConfiguration createMergedContextConfiguration(Class<?> testClass) {
		SpringBootTestContextBootstrapper bootstrapper = new SpringBootTestContextBootstrapper();
		bootstrapper.setBootstrapContext(new DefaultBootstrapContext(testClass, new DefaultCacheAwareContextLoaderDelegate()));
		return bootstrapper.buildMergedContextConfiguration();
	}

}
