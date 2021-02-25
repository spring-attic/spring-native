package org.springframework.nativex;

/**
 * @author Sebastien Deleuze
 */
public class NativeException extends RuntimeException {

	public NativeException() {
	}

	public NativeException(String message) {
		super(message);
	}

	public NativeException(String message, Throwable cause) {
		super(message, cause);
	}

	public NativeException(Throwable cause) {
		super(cause);
	}
}
