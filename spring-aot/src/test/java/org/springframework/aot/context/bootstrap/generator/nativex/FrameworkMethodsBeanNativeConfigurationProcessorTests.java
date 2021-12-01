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

package org.springframework.aot.context.bootstrap.generator.nativex;

import java.lang.reflect.Method;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.DefaultNativeReflectionEntry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FrameworkMethodsBeanNativeConfigurationProcessor}.
 *
 * @author Sébastien Deleuze
 */
public class FrameworkMethodsBeanNativeConfigurationProcessorTests {

	@Test
	void registerMethodsForMappingAnnotations() throws NoSuchMethodException {
		NativeConfigurationRegistry registry = register(BeanInstanceDescriptor.of(FooController.class).build());
		Method fooMethod = FooController.class.getDeclaredMethod("foo");
		Method barMethod = AbstractController.class.getDeclaredMethod("bar");
		assertThat(registry.reflection().reflectionEntries()).hasSize(2)
				.anySatisfy(method(fooMethod)).anySatisfy(method(barMethod));
	}

	private NativeConfigurationRegistry register(BeanInstanceDescriptor descriptor) {
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new FrameworkMethodsBeanNativeConfigurationProcessor().process(descriptor, registry);
		return registry;
	}

	private Consumer<DefaultNativeReflectionEntry> method(Method method) {
		return (entry) -> {
			assertThat(entry.getType()).isEqualTo(method.getDeclaringClass());
			assertThat(entry.getAccess()).isEmpty();
			assertThat(entry.getConstructors()).isEmpty();
			assertThat(entry.getMethods()).singleElement().isEqualTo(method);
			assertThat(entry.getFields()).isEmpty();
		};
	}

	@Controller
	class FooController extends AbstractController {

		@GetMapping
		String foo() {
			return "foo";
		}

		@EventListener
		void listener() {
		}

		@Bean
		void bean() {
		}
	}

	class AbstractController {

		@GetMapping
		String bar() {
			return "bar";
		}
	}
}
