package org.springframework.aot.beans.factory;

import java.util.function.Consumer;

/**
 * A {@link Consumer} that allows to invoke code that throws a checked exception.
 *
 * @author Stephane Nicoll
 */
@FunctionalInterface
public interface SmartConsumer<T> extends Consumer<T> {

	void acceptWithException(T t) throws Exception;

	@Override
	default void accept(T t) {
		try {
			acceptWithException(t);
		}
		catch (RuntimeException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

}
