package org.springframework.aot.beans.factory;

import java.util.function.Function;

/**
 * A {@link Function} that allows to invoke code that throws a checked exception.
 *
 * @author Stephane Nicoll
 */
@FunctionalInterface
public interface SmartFunction<T, R> extends Function<T, R> {

	R applyWithException(T t) throws Exception;

	@Override
	default R apply(T t) {
		try {
			return applyWithException(t);
		}
		catch (RuntimeException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
}
