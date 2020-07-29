package org.springframework.graalvm.substitutions;

import java.util.function.BooleanSupplier;

public class OptimizeTomcat implements BooleanSupplier {

	@Override
	public boolean getAsBoolean() {
		try {
			Class.forName("org.apache.tomcat.util.descriptor.web.VersionRule");
			return false;
		} catch (ClassNotFoundException ex1) {
			return true;
		}
	}
}
