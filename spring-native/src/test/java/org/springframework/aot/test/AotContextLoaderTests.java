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

import org.junit.jupiter.api.Test;

import org.springframework.aot.test.boot.AotSpringBootConfigContextLoader;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.SmartContextLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Tests for {@link AotContextLoader}.
 *
 * @author Stephane Nicoll
 */
class AotContextLoaderTests {

	@Test
	void loadWithClassNameFindMatchingContextLoader() {
		AotContextLoader aotContextLoader = new AotContextLoader(TestMapping.class.getName());
		assertThat(aotContextLoader.getContextLoader(AotContextLoaderTests.class)).isNotNull()
				.isInstanceOf(AotSpringBootConfigContextLoader.class);
	}

	@Test
	void loadWithClassNameReturnNullForUnregisteredTest() {
		AotContextLoader aotContextLoader = new AotContextLoader(TestMapping.class.getName());
		assertThat(aotContextLoader.getContextLoader(Map.class)).isNull();
	}

	@Test
	void loadWithClassNameWithoutMethod() {
		assertThatIllegalStateException().isThrownBy(() -> new AotContextLoader(Map.class.getName()))
				.withMessageContaining("No getContextLoaders() method found on java.util.Map");
	}

	@Test
	void loadWithClassNameThatDoesNotExist() {
		assertThatIllegalStateException().isThrownBy(() -> new AotContextLoader("com.example.DoesNotExist"))
				.withCauseInstanceOf(ClassNotFoundException.class);
	}


	public static class TestMapping {

		public static Map<String, Supplier<SmartContextLoader>> getContextLoaders() {
			Map<String, Supplier<SmartContextLoader>> entries = new HashMap<>();
			entries.put(AotContextLoaderTests.class.getName(), () -> new AotSpringBootConfigContextLoader(TestApplicationContextInitializer.class));
			entries.put("com.example.SampleTests", () -> new AotSpringBootConfigContextLoader(TestApplicationContextInitializer.class));
			return entries;
		}
	}

	static class TestApplicationContextInitializer implements ApplicationContextInitializer<GenericApplicationContext> {

		@Override
		public void initialize(GenericApplicationContext applicationContext) {

		}
	}

}
