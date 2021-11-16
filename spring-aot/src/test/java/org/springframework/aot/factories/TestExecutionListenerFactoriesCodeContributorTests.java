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

package org.springframework.aot.factories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import org.springframework.aot.TypeSystemExtension;
import org.springframework.aot.build.context.BuildContext;
import org.springframework.aot.factories.fixtures.DemoTestExecutionListener;
import org.springframework.aot.factories.fixtures.TestFactory;
import org.springframework.aot.test.AotDependencyInjectionTestExecutionListener;
import org.springframework.core.type.classreading.TypeSystem;
import org.springframework.nativex.AotOptions;
import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.domain.reflect.MethodDescriptor;
import org.springframework.nativex.domain.reflect.ReflectionDescriptor;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

/**
 * Unit tests for {@link TestExecutionListenerFactoriesCodeContributor}.
 *
 * @author Sam Brannen
 */
@ExtendWith(TypeSystemExtension.class)
class TestExecutionListenerFactoriesCodeContributorTests {

	private static final String TEL = TestExecutionListener.class.getName();
	private static final String DEMO_TEL = DemoTestExecutionListener.class.getName();
	private static final String DITEL = DependencyInjectionTestExecutionListener.class.getName();
	private static final String AOT_DITEL = AotDependencyInjectionTestExecutionListener.class.getName();


	private final FactoriesCodeContributor contributor = new TestExecutionListenerFactoriesCodeContributor();


	@Test
	void shouldNotContributeIfNotTestExecutionListener(TypeSystem typeSystem) {
		SpringFactory factory = SpringFactory.resolve(TestFactory.class.getName(), DEMO_TEL, typeSystem);
		assertThat(this.contributor.canContribute(factory)).isFalse();
	}

	@Test
	void shouldContributeIfTestExecutionListener(TypeSystem typeSystem) {
		SpringFactory factory = SpringFactory.resolve(TEL, DEMO_TEL, typeSystem);
		assertThat(this.contributor.canContribute(factory)).isTrue();
	}

	@Test
	void shouldContributeFactoryNameAndReflectionConfig(TypeSystem typeSystem) {
		assertContributedFactoryNameAndReflectionConfig(typeSystem, DEMO_TEL, DEMO_TEL);
	}

	@Test
	void shouldReplaceStandardDitelWithAotDitel(TypeSystem typeSystem) {
		assertContributedFactoryNameAndReflectionConfig(typeSystem, DITEL, AOT_DITEL);
	}


	@SuppressWarnings("unchecked")
	private void assertContributedFactoryNameAndReflectionConfig(TypeSystem typeSystem, String registeredListener,
			String expectedListener) {

		CodeGenerator code = new CodeGenerator(new AotOptions());
		SpringFactory factory = SpringFactory.resolve(TEL, registeredListener, typeSystem);
		BuildContext buildContext = Mockito.mock(BuildContext.class);

		this.contributor.contribute(factory, code, buildContext);

		assertThat(code.generateStaticSpringFactories()).asString()
				.contains("names.add(TestExecutionListener.class, \"" + expectedListener + "\");");

		ArgumentCaptor<Consumer<ReflectionDescriptor>> consumerArgumentCaptor = ArgumentCaptor.forClass(Consumer.class);
		Mockito.verify(buildContext).describeReflection(consumerArgumentCaptor.capture());
		Consumer<ReflectionDescriptor> reflectionConsumer = consumerArgumentCaptor.getValue();

		ReflectionDescriptor reflectionDescriptor = Mockito.mock(ReflectionDescriptor.class);
		reflectionConsumer.accept(reflectionDescriptor);

		ArgumentCaptor<ClassDescriptor> classDescriptorArgumentCaptor = ArgumentCaptor.forClass(ClassDescriptor.class);
		Mockito.verify(reflectionDescriptor).add(classDescriptorArgumentCaptor.capture());
		ClassDescriptor classDescriptor = classDescriptorArgumentCaptor.getValue();
		assertThat(classDescriptor.getName()).isEqualTo(expectedListener);
		assertThat(classDescriptor.getMethods()).containsExactly(MethodDescriptor.defaultConstructor());
	}

}
