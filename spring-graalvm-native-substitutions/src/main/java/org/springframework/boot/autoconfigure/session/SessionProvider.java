package org.springframework.boot.autoconfigure.session;

import org.springframework.boot.diagnostics.FailureAnalyzer;

public abstract class SessionProvider {

	public static FailureAnalyzer getNonUniqueSessionRepositoryFailureAnalyzer() {
		return new NonUniqueSessionRepositoryFailureAnalyzer();
	}
}
