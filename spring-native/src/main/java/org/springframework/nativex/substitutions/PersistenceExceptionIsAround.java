package org.springframework.nativex.substitutions;

import java.util.function.BooleanSupplier;

public class PersistenceExceptionIsAround implements BooleanSupplier {

	@Override
	public boolean getAsBoolean() {
		try {
			Class.forName("javax.persistence.PersistenceException");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}
