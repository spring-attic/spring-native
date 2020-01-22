package org.springframework.internal.svm;

import java.util.function.BooleanSupplier;

import org.springframework.graal.support.ConfigOptions;

public class YamlSupportRemoved implements BooleanSupplier {

	@Override
	public boolean getAsBoolean() {
		return ConfigOptions.shouldRemoveYamlSupport();
	}

}
