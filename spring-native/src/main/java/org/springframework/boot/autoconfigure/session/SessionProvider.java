package org.springframework.boot.autoconfigure.session;

import org.springframework.boot.diagnostics.FailureAnalyzer;

// Due to package private class
public abstract class SessionProvider {

	public static FailureAnalyzer getNonUniqueSessionRepositoryFailureAnalyzer() {
		return new NonUniqueSessionRepositoryFailureAnalyzer();
	}
}
