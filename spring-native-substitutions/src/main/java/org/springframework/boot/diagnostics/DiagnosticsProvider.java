package org.springframework.boot.diagnostics;

import org.springframework.boot.SpringBootExceptionReporter;
import org.springframework.context.ConfigurableApplicationContext;

// Due to package private class
public abstract class DiagnosticsProvider {

	public static SpringBootExceptionReporter getFailureAnalyzers(ConfigurableApplicationContext context) {
		return new FailureAnalyzers(context);
	}
}
