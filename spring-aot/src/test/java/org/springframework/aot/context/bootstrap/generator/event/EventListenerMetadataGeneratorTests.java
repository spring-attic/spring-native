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

package org.springframework.aot.context.bootstrap.generator.event;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.context.bootstrap.generator.sample.event.AnotherEventListener;
import org.springframework.aot.context.bootstrap.generator.sample.event.SingleEventListener;
import org.springframework.aot.context.bootstrap.generator.test.CodeSnippet;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EventListenerMetadataGenerator}.
 *
 * @author Stephane Nicoll
 */
class EventListenerMetadataGeneratorTests {

	@Test
	void writeEventListenerMetadataWithDefaultFactoryAndNoParameter() {
		Method method = ReflectionUtils.findMethod(AnotherEventListener.class, "onRefresh");
		assertThat(generateCode(new EventListenerMetadataGenerator("test", AnotherEventListener.class, method, null))).isEqualTo(
				"EventListenerMetadata.forBean(\"test\", AnotherEventListener.class).annotatedMethod(\"onRefresh\")");
	}

	@Test
	void writeEventListenerMetadataWithDefaultFactoryAndParameter() {
		Method method = ReflectionUtils.findMethod(SingleEventListener.class, "onStartup", ApplicationStartedEvent.class);
		assertThat(generateCode(new EventListenerMetadataGenerator("test", SingleEventListener.class, method, null))).isEqualTo(
				"EventListenerMetadata.forBean(\"test\", SingleEventListener.class).annotatedMethod(\"onStartup\", ApplicationStartedEvent.class)");
	}

	@Test
	void registerEventListenerMetadataUsesMethod() {
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		Method method = ReflectionUtils.findMethod(AnotherEventListener.class, "onRefresh");
		new EventListenerMetadataGenerator("test", AnotherEventListener.class, method, null).registerReflectionMetadata(registry);
		assertThat(registry.reflection().getEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(AnotherEventListener.class);
			assertThat(entry.getMethods()).containsOnly(method);
			assertThat(entry.getFields()).isEmpty();
		});
	}

	private CodeSnippet generateCode(EventListenerMetadataGenerator generator) {
		return CodeSnippet.of(generator::writeEventListenerMetadata);
	}

}
