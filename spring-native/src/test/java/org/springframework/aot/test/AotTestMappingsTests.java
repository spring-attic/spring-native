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

package org.springframework.aot.test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import org.springframework.aot.test.boot.AotSpringBootConfigContextLoader;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.SmartContextLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Tests for {@link AotTestMappings}.
 *
 * @author Stephane Nicoll
 * @author Sam Brannen
 */
class AotTestMappingsTests {

	@Test
	void loadWithClassNameFindsMatchingContextLoader() {
		AotTestMappings aotTestMappings = new AotTestMappings(TestMapping.class.getName());
		assertThat(aotTestMappings.getContextLoader(AotTestMappingsTests.class))
				.isInstanceOf(AotSpringBootConfigContextLoader.class);
	}

	@Test
	void loadWithClassNameReturnsNullContextLoaderForUnregisteredTest() {
		AotTestMappings aotTestMappings = new AotTestMappings(TestMapping.class.getName());
		assertThat(aotTestMappings.getContextLoader(Map.class)).isNull();
	}

	@Test
	void loadWithClassNameFindsMatchingContextInitializerClass() {
		AotTestMappings aotTestMappings = new AotTestMappings(TestMapping.class.getName());
		assertThat(aotTestMappings.getContextInitializerClass(AotTestMappingsTests.class))
				.isEqualTo(TestApplicationContextInitializer.class);
	}

	@Test
	void loadWithClassNameReturnsNullContextInitializerClassForUnregisteredTest() {
		AotTestMappings aotTestMappings = new AotTestMappings(TestMapping.class.getName());
		assertThat(aotTestMappings.getContextInitializerClass(Map.class)).isNull();
	}

	@Test
	void loadWithClassNameWithoutContextLoadersMethod() {
		assertThatIllegalStateException().isThrownBy(() -> new AotTestMappings(Map.class.getName()))
				.withMessage("No getContextLoaders() method found on java.util.Map");
	}

	@Test
	void loadWithClassNameWithoutContextInitializersMethod() {
		String className = HalfBakedTestMapping.class.getName();
		assertThatIllegalStateException().isThrownBy(() -> new AotTestMappings(className))
				.withMessage("No getContextInitializers() method found on " + className);
	}

	@Test
	void loadWithClassNameThatDoesNotExist() {
		String className = "com.example.DoesNotExist";
		assertThatIllegalStateException().isThrownBy(() -> new AotTestMappings(className))
				.withCauseInstanceOf(ClassNotFoundException.class)
				.withMessageMatching("Failed to load .+ method in " + Pattern.quote(className));
	}

	@SuppressWarnings("unused")
	public static class TestMapping {

		public static Map<String, Supplier<SmartContextLoader>> getContextLoaders() {
			Map<String, Supplier<SmartContextLoader>> entries = new HashMap<>();
			entries.put(AotTestMappingsTests.class.getName(), () -> new AotSpringBootConfigContextLoader(TestApplicationContextInitializer.class));
			entries.put("com.example.SampleTests", () -> new AotSpringBootConfigContextLoader(TestApplicationContextInitializer.class));
			return entries;
		}

		public static Map<String, Class<? extends ApplicationContextInitializer<?>>> getContextInitializers() {
			Map<String, Class<? extends ApplicationContextInitializer<?>>> map = new HashMap<>();
			map.put(AotTestMappingsTests.class.getName(), TestApplicationContextInitializer.class);
			map.put("com.example.SampleTests", TestApplicationContextInitializer.class);
			return map;
		}

	}

	public static class HalfBakedTestMapping {

		@SuppressWarnings("unused")
		public static Map<String, Supplier<SmartContextLoader>> getContextLoaders() {
			return null;
		}
	}

	static class TestApplicationContextInitializer implements ApplicationContextInitializer<GenericApplicationContext> {

		@Override
		public void initialize(GenericApplicationContext applicationContext) {

		}
	}

}
