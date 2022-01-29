/*
 * Copyright 2019-2022 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.springframework.aot.test;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link EnabledOnNativeCondition} and {@link DisabledOnNativeCondition}.
 *
 * @author Tadaya Tsuyukubo
 * @author Sam Brannen
 */
class OnNativeConditionTests {

	@ParameterizedTest(name = "[{index}] {0}")
	@MethodSource({ "onJvm", "onNative" })
	void evaluateCondition(ExecutionCondition condition, boolean isDisabled, String expectedReason) {
		ExtensionContext context = mock(ExtensionContext.class);
		when(context.getElement()).thenReturn(Optional.of(mock(AnnotatedElement.class)));

		ConditionEvaluationResult result = condition.evaluateExecutionCondition(context);
		assertThat(result).isNotNull();
		assertThat(result.isDisabled()).as("condition is disabled").isEqualTo(isDisabled);
		assertThat(result.getReason()).get(STRING).matches(expectedReason);
	}

	static Stream<Arguments> onJvm() {
		EnabledOnNativeCondition enabledOnNative = new EnabledOnNativeCondition() {
			@Override
			boolean inNativeImage() {
				return false;
			}
		};
		DisabledOnNativeCondition disabledOnNative = new DisabledOnNativeCondition() {
			@Override
			boolean inNativeImage() {
				return false;
			}
		};
		return Stream.of(
				arguments(named("@EnabledOnNative condition on JVM", enabledOnNative),
						true, "^.+ is disabled on non native image$"),
				arguments(named("@DisabledOnNative condition on JVM", disabledOnNative),
						false, "^.+ is enabled on non native image$")
		);
	}

	static Stream<Arguments> onNative() {
		EnabledOnNativeCondition enabledOnNative = new EnabledOnNativeCondition() {
			@Override
			boolean inNativeImage() {
				return true;
			}
		};
		DisabledOnNativeCondition disabledOnNative = new DisabledOnNativeCondition() {
			@Override
			boolean inNativeImage() {
				return true;
			}
		};
		return Stream.of(
				arguments(named("@EnabledOnNative condition on native image", enabledOnNative),
						false, "^.+ is enabled on native image$"),
				arguments(named("@DisabledOnNative condition on native image", disabledOnNative),
						true, "^.+ is disabled on native image$")
		);
	}

}
