package org.springframework.boot.autoconfigure.r2dbc;

import org.springframework.boot.diagnostics.FailureAnalyzer;

// Due to package private class
public abstract class R2dbcProvider {

	public static FailureAnalyzer getConnectionFactoryBeanCreationFailureAnalyzer() {
		return new ConnectionFactoryBeanCreationFailureAnalyzer();
	}
}
