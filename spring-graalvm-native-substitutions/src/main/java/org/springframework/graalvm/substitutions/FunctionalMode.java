package org.springframework.graalvm.substitutions;

import java.util.function.Predicate;

import org.springframework.graalvm.support.Mode;

public class FunctionalMode implements Predicate<String> {

	private static Boolean isFunctionalMode = null;

	/*
	 * If working on this, also check inferencing code in ConfigOptions.
	 */
	@Override
	public boolean test(String type) {
		if (isFunctionalMode == null) {
			String modeSet = System.getProperty("spring.native.mode");
			if (modeSet != null) {
				isFunctionalMode = modeSet.equalsIgnoreCase(Mode.FUNCTIONAL.name());
			} else {
				System.out.println("FM: mode not set, searching for resources...");
				if (exists("org.springframework.init.func.InfrastructureInitializer")
						|| exists("org.springframework.fu.kofu.KofuApplication")
						|| exists("org.springframework.fu.jafu.JafuApplication")) {
					isFunctionalMode = true;
				} else {
					isFunctionalMode = false;
				}
			}
		}
		return isFunctionalMode;
	}

	private boolean exists(String typename) {
		try {
			return Class.forName(typename, false, getClass().getClassLoader()) != null;
		} catch (ClassNotFoundException | NoClassDefFoundError ex) {
			return false;
		}
	}
}
