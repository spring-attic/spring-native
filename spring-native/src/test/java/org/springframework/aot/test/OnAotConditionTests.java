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
 * Unit tests for {@link EnabledOnAotCondition} and {@link DisabledOnAotCondition}.
 *
 * @author Tadaya Tsuyukubo
 * @author Sam Brannen
 */
class OnAotConditionTests {

	@ParameterizedTest(name = "[{index}] {0}")
	@MethodSource({ "aot", "nonAot" })
	void evaluateCondition(ExecutionCondition condition, boolean isDisabled, String expectedReason) {
		ExtensionContext context = mock(ExtensionContext.class);
		when(context.getElement()).thenReturn(Optional.of(mock(AnnotatedElement.class)));

		ConditionEvaluationResult result = condition.evaluateExecutionCondition(context);
		assertThat(result).isNotNull();
		assertThat(result.isDisabled()).as("condition is disabled").isEqualTo(isDisabled);
		assertThat(result.getReason()).get(STRING).matches(expectedReason);
	}

	static Stream<Arguments> aot() {
		EnabledOnAotCondition enabledOnAot = new EnabledOnAotCondition() {
			@Override
			boolean isRunningAotTests() {
				return true;
			}
		};
		DisabledOnAotCondition disabledOnAot = new DisabledOnAotCondition() {
			@Override
			boolean isRunningAotTests() {
				return true;
			}
		};
		return Stream.of(
				arguments(named("@EnabledOnAot condition on AOT mode", enabledOnAot),
						false, "^.+ is enabled on aot mode$"),
				arguments(named("@DisabledOnAot condition on AOT mode", disabledOnAot),
						true, "^.+ is disabled on aot mode$")
		);
	}

	static Stream<Arguments> nonAot() {
		EnabledOnAotCondition enabledOnAot = new EnabledOnAotCondition() {
			@Override
			boolean isRunningAotTests() {
				return false;
			}
		};
		DisabledOnAotCondition disabledOnAot = new DisabledOnAotCondition() {
			@Override
			boolean isRunningAotTests() {
				return false;
			}
		};
		return Stream.of(
				arguments(named("@EnabledOnAot condition on non-AOT mode", enabledOnAot),
						true, "^.+ is disabled on non aot mode$"),
				arguments(named("@DisabledOnAot condition on non-AOT mode", disabledOnAot),
						false, "^.+ is enabled on non aot mode$")
		);
	}

}
