/*
 * Copyright 2019-2022 the original author or authors.
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

package org.springframework.aot.test.boot;

import java.util.Set;

import com.squareup.javapoet.ClassName;
import org.junit.jupiter.api.Test;

import org.springframework.aot.test.context.bootstrap.generator.test.CodeSnippet;
import org.springframework.aot.test.samples.app.SampleApplicationIntegrationTests;
import org.springframework.aot.test.samples.app.SampleApplicationTests;
import org.springframework.aot.test.samples.app.slice.SampleJdbcTests;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.test.context.ReactiveWebMergedContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTestArgsAccessor;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContextBootstrapper;
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate;
import org.springframework.test.context.support.DefaultBootstrapContext;
import org.springframework.test.context.support.DefaultTestContextBootstrapper;
import org.springframework.test.context.web.WebMergedContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link SpringBootAotTestContextProcessor}.
 *
 * @author Stephane Nicoll
 */
class SpringBootAotTestContextProcessorTests {

	private final SpringBootAotTestContextProcessor processor = new SpringBootAotTestContextProcessor();

	@Test
	void supportsWithSpringBootTest() {
		TestContextBootstrapper bootstrapper = createSpringBootTestContextBootstrapper(SampleApplicationTests.class);
		assertThat(this.processor.supports(bootstrapper)).isTrue();
	}

	@Test
	void supportsWithSpringBootSliceTest() {
		TestContextBootstrapper bootstrapper = createSpringBootTestContextBootstrapper(SampleJdbcTests.class);
		assertThat(this.processor.supports(bootstrapper)).isTrue();
	}

	@Test
	void supportsWithNonSpringBootTest() {
		assertThat(this.processor.supports(new DefaultTestContextBootstrapper())).isFalse();
	}

	@Test
	void prepareTestContextForSpringBootTest() {
		TestContextBootstrapper bootstrapper = createSpringBootTestContextBootstrapper(SampleApplicationTests.class);
		GenericApplicationContext context = this.processor.prepareTestContext(bootstrapper.buildMergedContextConfiguration());
		assertThat(context).isNotNull();
		assertThat(context.isRunning()).isFalse();
		assertThat(context.getEnvironment()).isNotNull();
		assertThat(context.getBeanDefinitionNames()).contains("sampleApplication");
	}

	@Test
	void prepareTestContextForSpringBootSliceTest() {
		TestContextBootstrapper bootstrapper = createSpringBootTestContextBootstrapper(SampleJdbcTests.class);
		GenericApplicationContext context = this.processor.prepareTestContext(bootstrapper.buildMergedContextConfiguration());
		assertThat(context).isNotNull();
		assertThat(context.isRunning()).isFalse();
		assertThat(context.getEnvironment()).isNotNull();
		assertThat(context.getBeanDefinitionNames()).contains("sampleApplication");
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
	void writeInstanceSupplierForNonWebSpringBootTest() {
		MergedContextConfiguration contextConfiguration = mock(MergedContextConfiguration.class);
		given(contextConfiguration.getTestClass()).willAnswer((context) -> SampleApplicationTests.class);
		assertThat(CodeSnippet.of(this.processor.writeInstanceSupplier(contextConfiguration, ClassName.get("com.example", "Test"))))
				.hasImport(AotSpringBootConfigContextLoader.class)
				.isEqualTo("() -> new AotSpringBootConfigContextLoader(com.example.Test.class)");
	}

	@Test
	void writeInstanceSupplierForServletWebSpringBootTest() {
		WebMergedContextConfiguration contextConfiguration = mock(WebMergedContextConfiguration.class);
		given(contextConfiguration.getTestClass()).willAnswer((context) -> SampleApplicationTests.class);
		assertThat(CodeSnippet.of(this.processor.writeInstanceSupplier(contextConfiguration, ClassName.get("com.example", "Test"))))
				.hasImport(AotSpringBootConfigContextLoader.class).hasImport(WebApplicationType.class)
				.isEqualTo("() -> new AotSpringBootConfigContextLoader(com.example.Test.class, WebApplicationType.SERVLET, SpringBootTest.WebEnvironment.MOCK)");
	}

	@Test
	void writeInstanceSupplierForServletWebSpringBootTestWithRandomPort() {
		WebMergedContextConfiguration contextConfiguration = mock(WebMergedContextConfiguration.class);
		given(contextConfiguration.getTestClass()).willAnswer((context) -> SampleApplicationIntegrationTests.class);
		assertThat(CodeSnippet.of(this.processor.writeInstanceSupplier(contextConfiguration, ClassName.get("com.example", "Test"))))
				.hasImport(AotSpringBootConfigContextLoader.class)
				.isEqualTo("() -> new AotSpringBootConfigContextLoader(com.example.Test.class, WebApplicationType.SERVLET, SpringBootTest.WebEnvironment.RANDOM_PORT)");
	}

	@Test
	void writeInstanceSupplierForReactiveWebSpringBootTest() {
		ReactiveWebMergedContextConfiguration contextConfiguration = mock(ReactiveWebMergedContextConfiguration.class);
		given(contextConfiguration.getTestClass()).willAnswer((context) -> SampleApplicationTests.class);
		assertThat(CodeSnippet.of(this.processor.writeInstanceSupplier(contextConfiguration, ClassName.get("com.example", "Test"))))
				.hasImport(AotSpringBootConfigContextLoader.class)
				.isEqualTo("() -> new AotSpringBootConfigContextLoader(com.example.Test.class, WebApplicationType.REACTIVE, SpringBootTest.WebEnvironment.MOCK)");
	}

	@Test
	void writeInstanceSupplierForNonWebWithArguments() {
		Set<ContextCustomizer> customizers = Set.of(SpringBootTestArgsAccessor.create(SampleApplicationWithArgumentsTests.class));
		MergedContextConfiguration contextConfiguration = mock(MergedContextConfiguration.class);
		given(contextConfiguration.getTestClass()).willAnswer((context) -> SampleApplicationTests.class);
		given(contextConfiguration.getContextCustomizers()).willAnswer((context) -> customizers);
		assertThat(CodeSnippet.of(this.processor.writeInstanceSupplier(contextConfiguration, ClassName.get("com.example", "Test"))))
				.hasImport(AotSpringBootConfigContextLoader.class)
				.isEqualTo("() -> new AotSpringBootConfigContextLoader(com.example.Test.class, \"--app.test=one\", \"--app.name=foo\")");
	}

	@Test
	void writeInstanceSupplierForServletWithArguments() {
		Set<ContextCustomizer> customizers = Set.of(SpringBootTestArgsAccessor.create(SampleApplicationWithArgumentsTests.class));
		WebMergedContextConfiguration contextConfiguration = mock(WebMergedContextConfiguration.class);
		given(contextConfiguration.getTestClass()).willAnswer((context) -> SampleApplicationTests.class);
		given(contextConfiguration.getContextCustomizers()).willAnswer((context) -> customizers);
		assertThat(CodeSnippet.of(this.processor.writeInstanceSupplier(contextConfiguration, ClassName.get("com.example", "Test"))))
				.hasImport(AotSpringBootConfigContextLoader.class).hasImport(WebApplicationType.class)
				.isEqualTo("() -> new AotSpringBootConfigContextLoader(com.example.Test.class, WebApplicationType.SERVLET, SpringBootTest.WebEnvironment.MOCK, \"--app.test=one\", \"--app.name=foo\")");
	}

	private TestContextBootstrapper createSpringBootTestContextBootstrapper(Class<?> testClass) {
		SpringBootTestContextBootstrapper bootstrapper = new SpringBootTestContextBootstrapper();
		bootstrapper.setBootstrapContext(new DefaultBootstrapContext(testClass, new DefaultCacheAwareContextLoaderDelegate()));
		return bootstrapper;
	}

	@SpringBootTest(args = { "--app.test=one", "--app.name=foo" })
	static class SampleApplicationWithArgumentsTests {

	}

}
