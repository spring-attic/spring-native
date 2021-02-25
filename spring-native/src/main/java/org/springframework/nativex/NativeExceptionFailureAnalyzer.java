package org.springframework.nativex;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * @author Sebastien Deleuze
 */
public class NativeExceptionFailureAnalyzer extends AbstractFailureAnalyzer<NativeException> {

	private static final String MISSING_MANDATORY_CLASS = "Mandatory generated class";

	@Override
	protected FailureAnalysis analyze(Throwable rootFailure, NativeException cause) {
		if (cause.getMessage().startsWith(MISSING_MANDATORY_CLASS)) {
			return new FailureAnalysis(cause.getMessage(),  "Please make sure spring-aot-maven-plugin or " +
					"spring-aot-gradle-plugin are properly configured, and that code generation has been properly performed. " +
					"See https://github.com/spring-projects-experimental/spring-native/blob/master/spring-native-docs/src/main/asciidoc/spring-aot-build-plugins.adoc " +
					"for more details.", cause);
		}
		return null;
	}
}
