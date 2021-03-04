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
					+ "See https://docs.spring.io/spring-native/docs/current/reference/htmlsingle/#spring-aot for more details.");

	@Override
	protected FailureAnalysis analyze(Throwable rootFailure, GeneratedClassNotFoundException ex) {
		return new FailureAnalysis(ex.getMessage(), ACTION, ex);
	}

}
