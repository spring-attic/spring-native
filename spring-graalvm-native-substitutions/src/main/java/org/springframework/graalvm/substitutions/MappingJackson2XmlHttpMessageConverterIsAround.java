package org.springframework.graalvm.substitutions;

import java.util.function.BooleanSupplier;

public class MappingJackson2XmlHttpMessageConverterIsAround implements BooleanSupplier {

	@Override
	public boolean getAsBoolean() {
		try {
			Class.forName("org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

}
