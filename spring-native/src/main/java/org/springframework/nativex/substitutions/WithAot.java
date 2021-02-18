package org.springframework.nativex.substitutions;

import java.util.function.BooleanSupplier;

public class WithAot implements BooleanSupplier {

	@Override
	public boolean getAsBoolean() {
		try {
			Class.forName("org.springframework.aot.StaticSpringFactories");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

}