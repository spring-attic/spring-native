package org.springframework.boot.autoconfigure.flyway;

import org.springframework.boot.diagnostics.FailureAnalyzer;

public abstract class FlywayProvider {

	public static FailureAnalyzer getFlywayMigrationScriptMissingFailureAnalyzer() {
		return new FlywayMigrationScriptMissingFailureAnalyzer();
	}
}
