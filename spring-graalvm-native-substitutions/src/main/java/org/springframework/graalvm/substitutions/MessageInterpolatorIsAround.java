package org.springframework.graalvm.substitutions;

import java.util.function.BooleanSupplier;

public class MessageInterpolatorIsAround implements BooleanSupplier {

	@Override
	public boolean getAsBoolean() {
		try {
			Class.forName("javax.validation.MessageInterpolator");
			Class.forName("org.springframework.boot.validation.MessageInterpolatorFactory");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

}
