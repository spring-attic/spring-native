package org.springframework.boot.autoconfigure.diagnostics.analyzer;

import org.springframework.boot.diagnostics.FailureAnalyzer;

// Due to package private class
public abstract class AutoconfigureAnalyzerProvider {

	public static FailureAnalyzer getNoSuchBeanDefinitionFailureAnalyzer() {
		return new NoSuchBeanDefinitionFailureAnalyzer();
	}
}
