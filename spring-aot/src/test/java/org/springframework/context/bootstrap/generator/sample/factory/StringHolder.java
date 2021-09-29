package org.springframework.context.bootstrap.generator.sample.factory;

public class StringHolder {

	private final String message;

	public StringHolder(String message) {
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}

}
