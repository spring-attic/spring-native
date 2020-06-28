package org.springframework.graalvm.substitutions;

import java.util.function.Predicate;

public class FunctionalMode implements Predicate<String> {

	@Override
	public boolean test(String type) {
		return System.getProperty("spring.native.mode", "default").equals("functional");
	}
}
