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

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.test.samples.app.SampleApplication;
import org.springframework.aot.test.samples.app.SampleApplicationAnotherTests;
import org.springframework.aot.test.samples.app.SampleApplicationRestClientTests;
import org.springframework.aot.test.samples.app.SampleApplicationTests;
import org.springframework.aot.test.samples.app.slice.SampleJdbcTests;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.nativex.hint.Flag;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextLoader;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContextBootstrapper;
import org.springframework.test.context.support.AbstractTestContextBootstrapper;
import org.springframework.test.context.support.DelegatingSmartContextLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Tests for {@link TestContextConfigurationDescriptorFactory}.
 *
 * @author Stephane Nicoll
 * @author Sebastien Deleuze
 */
class TestContextConfigurationDescriptorFactoryTests {

	private final TestContextConfigurationDescriptorFactory factory = new TestContextConfigurationDescriptorFactory(getClass().getClassLoader());

	private final NativeConfigurationRegistry nativeConfigurationRegistry = new NativeConfigurationRegistry();

	@Test
	void createTestContextConfigurationDescriptorForSingleConfigurationOnMultipleTests() {
		assertThat(this.factory.buildConfigurationDescriptors(List.of(SampleApplicationTests.class, SampleApplicationAnotherTests.class), nativeConfigurationRegistry))
				.singleElement().satisfies((descriptor) -> assertThat(descriptor.getTestClasses())
						.containsOnly(SampleApplicationTests.class, SampleApplicationAnotherTests.class));
	}

	@Test
	void createTestContextConfigurationDescriptorForSeveralConfigurations() {
		List<TestContextConfigurationDescriptor> descriptors = this.factory.buildConfigurationDescriptors(List.of(
				SampleApplicationTests.class, SampleApplicationAnotherTests.class, SampleJdbcTests.class), nativeConfigurationRegistry);
		assertThat(descriptors).anySatisfy((descriptor -> assertThat(descriptor.getTestClasses())
				.containsOnly(SampleApplicationTests.class, SampleApplicationAnotherTests.class)));
		assertThat(descriptors).anySatisfy((descriptor -> assertThat(descriptor.getTestClasses())
				.containsOnly(SampleJdbcTests.class)));
		assertThat(descriptors).hasSize(2);
	}

	@Test
	void createTestContextConfigurationDescriptorForUnsupportedTestContextBootstrapper() {
		assertThatIllegalStateException().isThrownBy(() -> this.factory.buildConfigurationDescriptors(List.of(UnsupportedSpringTest.class),
				nativeConfigurationRegistry))
				.withMessageContaining("No processor found for")
				.withMessageContaining(UnsupportedTestContextBootstrapper.class.getName());
	}

	@Test
	void createTestContextBootstrapperForSpringBootTest() {
		TestContextBootstrapper testContextBootstrapper = this.factory.createTestContextBootstrapper(SampleApplicationTests.class, nativeConfigurationRegistry);
		assertThat(testContextBootstrapper).isNotNull();
		MergedContextConfiguration configuration = testContextBootstrapper.buildMergedContextConfiguration();
		assertThat(configuration).isNotNull();
		assertThat(configuration.getTestClass()).isEqualTo(SampleApplicationTests.class);
		assertThat(configuration.getClasses()).containsOnly(SampleApplication.class);
		assertThat(nativeConfigurationRegistry.reflection().reflectionEntries()).singleElement().satisfies(entry -> {
			assertThat(entry.getType()).isEqualTo(SpringBootTestContextBootstrapper.class);
			assertThat(entry.getFlags()).singleElement().isEqualTo(Flag.allDeclaredConstructors);
		});
	}

	@Test
	void createTestContextBootstrapperForSpringBootTestWithCustomBootstrapper() {
		TestContextBootstrapper testContextBootstrapper = this.factory.createTestContextBootstrapper(SampleApplicationRestClientTests.class, nativeConfigurationRegistry);
		assertThat(testContextBootstrapper).isNotNull();
		MergedContextConfiguration configuration = testContextBootstrapper.buildMergedContextConfiguration();
		assertThat(configuration).isNotNull();
		assertThat(configuration.getTestClass()).isEqualTo(SampleApplicationRestClientTests.class);
		assertThat(configuration.getClasses()).containsOnly(SampleApplication.class);
		assertThat(nativeConfigurationRegistry.reflection().reflectionEntries()).singleElement().satisfies(entry -> {
			assertThat(entry.getType().getName()).isEqualTo("org.springframework.boot.test.autoconfigure.web.client.RestClientTestContextBootstrapper");
			assertThat(entry.getFlags()).singleElement().isEqualTo(Flag.allDeclaredConstructors);
		});
	}

	@Test
	void createTestContextBootstrapForNonSpringTest() {
		assertThatIllegalArgumentException().isThrownBy(() -> this.factory.createTestContextBootstrapper(String.class, nativeConfigurationRegistry))
				.withMessageContaining(String.class.getName())
				.withMessageContaining("is not a Spring test class, @BootstrapWith annotation not found");
	}

	@Test
	void parseTestContextInvokeAotTestContextProcessor() {
		GenericApplicationContext context = new GenericApplicationContext();
		AotTestContextProcessor processor = mockAotTestContextProcessor((bootstrapper) -> true, () -> context);
		TestContextConfigurationDescriptor descriptor = new TestContextConfigurationDescriptorFactory(List.of(processor))
				.buildConfigurationDescriptors(List.of(SampleApplicationTests.class), nativeConfigurationRegistry).get(0);
		verify(processor).supports(any());
		assertThat(descriptor.parseTestContext()).isSameAs(context);
		verify(processor).prepareTestContext(any());
	}

	@Test
	@SuppressWarnings("resource")
	void supportsSearchAllAvailableAotProcessors() {
		AotTestContextProcessor notSupportedProcessor = mockAotTestContextProcessor((bootstrapper) -> false, () -> null);
		AotTestContextProcessor anotherNotSupportedProcessor = mockAotTestContextProcessor((bootstrapper) -> false, () -> null);
		GenericApplicationContext context = new GenericApplicationContext();
		AotTestContextProcessor processor = mockAotTestContextProcessor((bootstrapper) -> true, () -> context);
		AotTestContextProcessor yetAnotherNotSupportedProcessor = mockAotTestContextProcessor((bootstrapper) -> false, () -> null);

		new TestContextConfigurationDescriptorFactory(
				List.of(notSupportedProcessor, anotherNotSupportedProcessor, processor, yetAnotherNotSupportedProcessor))
				.buildConfigurationDescriptors(List.of(SampleApplicationTests.class), nativeConfigurationRegistry);
		InOrder ordered = inOrder(notSupportedProcessor, anotherNotSupportedProcessor, processor);
		ordered.verify(notSupportedProcessor).supports(any());
		ordered.verify(anotherNotSupportedProcessor).supports(any());
		ordered.verify(processor).supports(any());
		verifyNoInteractions(yetAnotherNotSupportedProcessor);
	}

	private AotTestContextProcessor mockAotTestContextProcessor(Predicate<TestContextBootstrapper> supportsPredicate,
			Supplier<GenericApplicationContext> contextSupplier) {
		AotTestContextProcessor processor = mock(AotTestContextProcessor.class);
		given(processor.supports(any())).willAnswer((invocation) -> supportsPredicate.test(invocation.getArgument(0)));
		given(processor.prepareTestContext(any())).willReturn(contextSupplier.get());
		return processor;
	}


	@BootstrapWith(UnsupportedTestContextBootstrapper.class)
	static class UnsupportedSpringTest {

	}


	static class UnsupportedTestContextBootstrapper extends AbstractTestContextBootstrapper {

		@Override
		protected Class<? extends ContextLoader> getDefaultContextLoaderClass(Class<?> testClass) {
			return DelegatingSmartContextLoader.class;
		}
	}

}
