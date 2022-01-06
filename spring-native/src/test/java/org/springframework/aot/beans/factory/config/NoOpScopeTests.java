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

package org.springframework.aot.beans.factory.config;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.ObjectFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link NoOpScope}.
 *
 * @author Stephane Nicoll
 */
class NoOpScopeTests {

	private final NoOpScope scope = new NoOpScope();

	@Test
	void getApplySingletonScope() {
		ObjectFactory<?> factory = mockObjectFactory(() -> "instance");
		assertThat(scope.get("test", factory)).isEqualTo("instance");
		assertThat(scope.get("test", factory)).isEqualTo("instance");
		verify(factory).getObject();
	}

	@Test
	void removeApplySingletonScope() {
		scope.get("test", () -> "something");
		scope.remove("test");
		ObjectFactory<?> factory = mockObjectFactory(() -> "instance");
		assertThat(scope.get("test", factory)).isEqualTo("instance");
		verify(factory).getObject();
	}

	@Test
	void destructionCallbacksAreInvoked() {
		Runnable runnable = mock(Runnable.class);
		scope.registerDestructionCallback("test", runnable);
		this.scope.close();
		verify(runnable).run();
	}

	@SuppressWarnings("unchecked")
	private ObjectFactory<?> mockObjectFactory(Supplier<?> value) {
		ObjectFactory<Object> objectFactory = mock(ObjectFactory.class);
		given(objectFactory.getObject()).willReturn(value.get());
		return objectFactory;
	}

}
