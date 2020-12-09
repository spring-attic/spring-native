package org.springframework.nativex.buildtools;

/**
 * A {@link CodeGenerationException} is thrown when a {@link BootstrapContributor}
 * fails while generating boostrap resources for the application.
 *
 * @author Brian Clozel
 */
public class CodeGenerationException extends RuntimeException {

	public CodeGenerationException(String message) {
		super(message);
	}

	public CodeGenerationException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
