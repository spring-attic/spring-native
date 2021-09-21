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
package org.springframework.context.bootstrap.generator.nativex;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.Arrays;

import com.squareup.javapoet.CodeBlock;
import org.junit.jupiter.api.Test;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.InitializationCallback;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.context.bootstrap.generator.sample.callback.ConfigHavingBeansWithInitMethod.PrivateExternallyManagedInitMethod;
import org.springframework.context.bootstrap.generator.sample.callback.ConfigHavingBeansWithInitMethod.PublicExternallyManagedInitMethod;
import org.springframework.util.ReflectionUtils;

/**
 * Tests for {@link HintsBeanNativeConfigurationProcessor}.
 *
 * @author Christoph Strobl
 */
class HintsBeanNativeConfigurationProcessorTests {

	HintsBeanNativeConfigurationProcessor processor = new HintsBeanNativeConfigurationProcessor();

	@Test // GH-1048
	void registerReflectionEntriesPrivateInitMethod() {

		Method initMethod = ReflectionUtils.findMethod(PrivateExternallyManagedInitMethod.class, "privateInit");
		InitializationCallback initializationCallback = new InitializationCallback(initMethod, var -> CodeBlock.builder().build());
		NativeConfigurationRegistry registry = register(BeanInstanceDescriptor.of(PrivateExternallyManagedInitMethod.class)
				.withInitializationCallbacks(Arrays.asList(initializationCallback)).build());

		assertThat(registry.reflection().getEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(PrivateExternallyManagedInitMethod.class);
			assertThat(entry.getMethods()).contains(initMethod);
			assertThat(entry.getConstructors()).isEmpty();
			assertThat(entry.getFields()).isEmpty();
		});
	}

	@Test // GH-1048
	void doesNotRegisterReflectionEntriesPublicInitMethod() {

		Method initMethod = ReflectionUtils.findMethod(PublicExternallyManagedInitMethod.class, "publicInit");
		InitializationCallback initializationCallback = new InitializationCallback(initMethod, var -> CodeBlock.builder().build());
		NativeConfigurationRegistry registry = register(BeanInstanceDescriptor.of(PublicExternallyManagedInitMethod.class)
				.withInitializationCallbacks(Arrays.asList(initializationCallback)).build());

		assertThat(registry.reflection().getEntries()).isEmpty();
	}

	private NativeConfigurationRegistry register(BeanInstanceDescriptor descriptor) {

		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		processor.process(descriptor, registry);
		return registry;
	}
}
