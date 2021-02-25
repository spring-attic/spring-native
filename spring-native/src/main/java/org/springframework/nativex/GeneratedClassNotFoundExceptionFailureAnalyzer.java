package org.springframework.nativex;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * @author Sebastien Deleuze
 */
public class GeneratedClassNotFoundExceptionFailureAnalyzer extends AbstractFailureAnalyzer<GeneratedClassNotFoundException> {

	@Override
	protected FailureAnalysis analyze(Throwable rootFailure, GeneratedClassNotFoundException cause) {
		return new FailureAnalysis(cause.getMessage(),  "See https://github.com/spring-projects-experimental/spring-native/blob/master/spring-native-docs/src/main/asciidoc/spring-aot-build-plugins.adoc " +
				"for more details.", cause);
	}
}
