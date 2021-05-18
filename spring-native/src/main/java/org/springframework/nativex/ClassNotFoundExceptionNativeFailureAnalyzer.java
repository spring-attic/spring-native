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

package org.springframework.nativex;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.core.NativeDetector;

/**
 * Failure analyzer for missing native configuration which results in {@link ClassNotFoundException}.
 *
 * @author Sebastien Deleuze
 */
public class ClassNotFoundExceptionNativeFailureAnalyzer extends AbstractFailureAnalyzer<ClassNotFoundException>  {

	private static final String ACTION = String.format(
			"Native configuration for a class accessed reflectively is likely missing.%n"
					+ "You can try to configure native hints in order to specify it explicitly.%n"
					+ "See https://docs.spring.io/spring-native/docs/current/reference/htmlsingle/#native-hints for more details.");

	@Override
	protected FailureAnalysis analyze(Throwable rootFailure, ClassNotFoundException ex) {
		if (NativeDetector.inNativeImage()) {
			return new FailureAnalysis("Native reflection configuration for " + ex.getMessage() + " is missing.", ACTION, ex);
		}
		return null;
	}
}
