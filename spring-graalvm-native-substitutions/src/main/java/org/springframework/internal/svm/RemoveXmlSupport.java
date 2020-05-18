package org.springframework.internal.svm;

import java.util.function.BooleanSupplier;

public class RemoveXmlSupport implements BooleanSupplier {

	@Override
	public boolean getAsBoolean() {
		return Boolean.valueOf(System.getProperty("spring.native.remove-xml-support", "false"));
	}

}
