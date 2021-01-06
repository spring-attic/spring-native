package org.springframework.boot.autoconfigure.jdbc;

import org.springframework.boot.diagnostics.FailureAnalyzer;

public abstract class JdbcProvider {

	public static FailureAnalyzer getDataSourceBeanCreationFailureAnalyzer() {
		return new DataSourceBeanCreationFailureAnalyzer();
	}

	public static FailureAnalyzer getHikariDriverConfigurationFailureAnalyzer() {
		return new HikariDriverConfigurationFailureAnalyzer();
	}
}
