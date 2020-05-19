package org.springframework.graalvm.substitutions;

import java.util.function.BooleanSupplier;

public class RemoveCglibSupport implements BooleanSupplier {

	@Override
	public boolean getAsBoolean() {
		return Boolean.valueOf(System.getProperty("spring.native.remove-cglib-support", "true"));
	}

}
