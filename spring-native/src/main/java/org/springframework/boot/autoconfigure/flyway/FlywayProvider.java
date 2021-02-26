package org.springframework.boot.autoconfigure.flyway;

import org.springframework.boot.diagnostics.FailureAnalyzer;

// Due to package private class
public abstract class FlywayProvider {

	public static FailureAnalyzer getFlywayMigrationScriptMissingFailureAnalyzer() {
		return new FlywayMigrationScriptMissingFailureAnalyzer();
	}
}
