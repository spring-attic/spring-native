package org.springframework.boot.diagnostics.analyzer;

import org.springframework.boot.diagnostics.FailureAnalyzer;

// Due to package private class
public abstract class AnalyzerProvider {

	public static FailureAnalyzer getBeanCurrentlyInCreationFailureAnalyzer() {
		return new BeanCurrentlyInCreationFailureAnalyzer();
	}

	public static FailureAnalyzer getBeanDefinitionOverrideFailureAnalyzer() {
		return new BeanDefinitionOverrideFailureAnalyzer();
	}

	public static FailureAnalyzer getBeanNotOfRequiredTypeFailureAnalyzer() {
		return new BeanNotOfRequiredTypeFailureAnalyzer();
	}

	public static FailureAnalyzer getBindFailureAnalyzer() {
		return new BindFailureAnalyzer();
	}

	public static FailureAnalyzer getBindValidationFailureAnalyzer() {
		return new BindValidationFailureAnalyzer();
	}

	public static FailureAnalyzer getUnboundConfigurationPropertyFailureAnalyzer() {
		return new UnboundConfigurationPropertyFailureAnalyzer();
	}

	public static FailureAnalyzer getConnectorStartFailureAnalyzer() {
		return new ConnectorStartFailureAnalyzer();
	}

	public static FailureAnalyzer getNoSuchMethodFailureAnalyzer() {
		return new NoSuchMethodFailureAnalyzer();
	}

	public static FailureAnalyzer getNoUniqueBeanDefinitionFailureAnalyzer() {
		return new NoUniqueBeanDefinitionFailureAnalyzer();
	}

	public static FailureAnalyzer getPortInUseFailureAnalyzer() {
		return new PortInUseFailureAnalyzer();
	}

	public static FailureAnalyzer getValidationExceptionFailureAnalyzer() {
		return new ValidationExceptionFailureAnalyzer();
	}

	public static FailureAnalyzer getInvalidConfigurationPropertyNameFailureAnalyzer() {
		return new InvalidConfigurationPropertyNameFailureAnalyzer();
	}

	public static FailureAnalyzer getInvalidConfigurationPropertyValueFailureAnalyzer() {
		return new InvalidConfigurationPropertyValueFailureAnalyzer();
	}
}
