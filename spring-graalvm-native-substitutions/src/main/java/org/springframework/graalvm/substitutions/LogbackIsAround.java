package org.springframework.graalvm.substitutions;

import java.util.function.BooleanSupplier;

public class LogbackIsAround implements BooleanSupplier {

	@Override
	public boolean getAsBoolean() {
		try {
			Class.forName("ch.qos.logback.classic.LoggerContext");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

}
