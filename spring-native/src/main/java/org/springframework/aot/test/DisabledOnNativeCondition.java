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

package org.springframework.aot.test;

import java.lang.reflect.AnnotatedElement;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import org.springframework.core.NativeDetector;
import org.springframework.util.Assert;

import static java.lang.String.format;

/**
 * {@link ExecutionCondition} for {@link DisabledOnNative @DisabledOnNative}.
 *
 * @author Tadaya Tsuyukubo
 * @author Sam Brannen
 */
class DisabledOnNativeCondition implements ExecutionCondition {

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		Assert.state(context.getElement().isPresent(), "No AnnotatedElement");
		AnnotatedElement element = context.getElement().get();

		if (inNativeImage()) {
			return ConditionEvaluationResult.disabled(format("%s is disabled on native image", element));
		}
		else {
			return ConditionEvaluationResult.enabled(format("%s is enabled on non native image", element));
		}
	}

	/**
	 * Determine if we are running in a native image.
	 *
	 * <p>The default implementation delegates to
	 * {@link NativeDetector#inNativeImage()}. Can be overridden in a subclass for
	 * testing purposes.
	 */
	boolean inNativeImage() {
		return NativeDetector.inNativeImage();
	}

}
