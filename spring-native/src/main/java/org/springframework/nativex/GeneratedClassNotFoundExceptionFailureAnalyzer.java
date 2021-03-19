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

/**
 * Failure analyzer for {@link GeneratedClassNotFoundException}.
 *
 * @author Sebastien Deleuze
 */
public class GeneratedClassNotFoundExceptionFailureAnalyzer extends AbstractFailureAnalyzer<GeneratedClassNotFoundException> {

	private static final String ACTION = String.format(
			"Review your local configuration and make sure that the Spring AOT plugin is configured properly.%n"
					+ "If you're trying to run your application with 'mvn spring-boot:run', please use 'mvn package spring-boot:run' instead.%n"
					+ "See https://docs.spring.io/spring-native/docs/current/reference/htmlsingle/#spring-aot for more details.");

	@Override
	protected FailureAnalysis analyze(Throwable rootFailure, GeneratedClassNotFoundException ex) {
		return new FailureAnalysis(ex.getMessage(), ACTION, ex);
	}

}
