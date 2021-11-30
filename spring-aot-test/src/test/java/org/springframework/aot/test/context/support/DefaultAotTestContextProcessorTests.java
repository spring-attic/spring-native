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

package org.springframework.aot.test.context.support;

import com.squareup.javapoet.ClassName;
import org.junit.jupiter.api.Test;

import org.springframework.aot.test.context.bootstrap.generator.test.CodeSnippet;
import org.springframework.aot.test.samples.simple.SimpleSpringTests;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.ContextLoader;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContextBootstrapper;
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate;
import org.springframework.test.context.support.AbstractTestContextBootstrapper;
import org.springframework.test.context.support.DefaultBootstrapContext;
import org.springframework.test.context.support.DefaultTestContextBootstrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link DefaultAotTestContextProcessor}.
 *
 * @author Stephane Nicoll
 */
class DefaultAotTestContextProcessorTests {

	private final DefaultAotTestContextProcessor processor = new DefaultAotTestContextProcessor();

	@Test
	void supportsWithSpringJUnitConfig() {
		TestContextBootstrapper bootstrapper = createDefaultTestContextBootstrapper(SimpleSpringTests.class);
		assertThat(this.processor.supports(bootstrapper)).isTrue();
	}

	@Test
	void supportsWithNonDefaultBootstrapper() {
		TestContextBootstrapper customTestContextBootstrapper = new AbstractTestContextBootstrapper() {
			@Override
			protected Class<? extends ContextLoader> getDefaultContextLoaderClass(Class<?> testClass) {
				return null;
			}
		};
		assertThat(this.processor.supports(customTestContextBootstrapper)).isFalse();
	}

	@Test
	void prepareTestContextForSpringJUnitConfig() {
		TestContextBootstrapper bootstrapper = createDefaultTestContextBootstrapper(SimpleSpringTests.class);
		GenericApplicationContext context = this.processor.prepareTestContext(bootstrapper.buildMergedContextConfiguration());
		assertThat(context).isNotNull();
		assertThat(context.isRunning()).isFalse();
		assertThat(context.getEnvironment()).isNotNull();
		assertThat(context.getBeanDefinitionNames()).contains("simpleSpringTests.TestConfiguration");
	}

	@Test
	void prepareTestContextForInvalidMergedConfiguration() {
		MergedContextConfiguration contextConfiguration = mock(MergedContextConfiguration.class);
		given(contextConfiguration.getLocations()).willReturn(new String[0]);
		given(contextConfiguration.getClasses()).willReturn(new Class<?>[0]);
		assertThatIllegalStateException().isThrownBy(() -> this.processor.prepareTestContext(contextConfiguration))
				.withMessageContaining("Failed to prepare test context");
	}

	@Test
	void writeInstanceSupplierForSpringJUnitConfigTest() {
		MergedContextConfiguration contextConfiguration = mock(MergedContextConfiguration.class);
		given(contextConfiguration.getTestClass()).willAnswer((context) -> SimpleSpringTests.class);
		assertThat(CodeSnippet.of(this.processor.writeInstanceSupplier(contextConfiguration, ClassName.get("com.example", "Test"))))
				.hasImport(AotDefaultConfigContextLoader.class)
				.isEqualTo("() -> new AotDefaultConfigContextLoader(com.example.Test.class)");
	}

	private TestContextBootstrapper createDefaultTestContextBootstrapper(Class<?> testClass) {
		DefaultTestContextBootstrapper bootstrapper = new DefaultTestContextBootstrapper();
		bootstrapper.setBootstrapContext(new DefaultBootstrapContext(testClass, new DefaultCacheAwareContextLoaderDelegate()));
		return bootstrapper;
	}

}
