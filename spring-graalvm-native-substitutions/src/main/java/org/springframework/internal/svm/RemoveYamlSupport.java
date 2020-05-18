package org.springframework.internal.svm;

import java.util.function.BooleanSupplier;

public class RemoveYamlSupport implements BooleanSupplier {

	@Override
	public boolean getAsBoolean() {
		return Boolean.valueOf(System.getProperty("spring.native.remove-yaml-support", "false"));
	}

}
