package org.springframework.boot.autoconfigure;

import org.springframework.context.ApplicationContextInitializer;

// Due to package private class
public abstract class AutoconfigureProvider {

	public static ApplicationContextInitializer getSharedMetadataReaderFactoryContextInitializer() {
		return new SharedMetadataReaderFactoryContextInitializer();
	}
}
