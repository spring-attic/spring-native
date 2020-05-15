package org.springframework.internal.svm;

import java.util.function.BooleanSupplier;

public class FormHttpMessageConverterIsAround implements BooleanSupplier {

	@Override
	public boolean getAsBoolean() {
		try {
			Class.forName("org.springframework.http.converter.FormHttpMessageConverter");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

}
