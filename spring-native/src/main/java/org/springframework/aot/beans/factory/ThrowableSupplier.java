package org.springframework.aot.beans.factory;

import java.util.function.Supplier;

/**
 * A {@link Supplier} that allows to invoke code that throws a checked exception.
 *
 * @author Stephane Nicoll
 */
public interface ThrowableSupplier<T> extends Supplier<T> {

	T getWithException() throws Exception;

	@Override
	default T get() {
		try {
			return getWithException();
		}
		catch (RuntimeException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
}
