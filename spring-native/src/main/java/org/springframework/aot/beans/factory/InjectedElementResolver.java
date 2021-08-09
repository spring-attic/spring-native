package org.springframework.aot.beans.factory;

import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.context.support.GenericApplicationContext;

/**
 * Resolve the attributes of an injected element such as a {@code Constructor} or a
 * factory {@code Method}.
 *
 * @author Stephane Nicoll
 */
public interface InjectedElementResolver {

	/**
	 * Resolve the attributes using the specified {@link GenericApplicationContext context}.
	 * @param context the context to use
	 * @return the resolved attributes
	 */
	default InjectedElementAttributes resolve(GenericApplicationContext context) {
		return resolve(context, true);
	}

	/**
	 * Resolve the attributes using the specified {@link GenericApplicationContext context}.
	 * @param context the context to use
	 * @param required whether the injection point is mandatory
	 * @return the resolved attributes
	 */
	InjectedElementAttributes resolve(GenericApplicationContext context, boolean required);

	/**
	 * Invoke the specified consumer with the resolved {@link InjectedElementAttributes
	 * attributes}.
	 * @param context the context to use to resolve the attributes
	 * @param attributes a consumer of the resolved attributes
	 */
	default void invoke(GenericApplicationContext context, SmartConsumer<InjectedElementAttributes> attributes) {
		InjectedElementAttributes elements = resolve(context);
		attributes.accept(elements);
	}

	/**
	 * Create an instance based on the resolved {@link InjectedElementAttributes
	 * attributes}.
	 * @param context the context to use to resolve the attributes
	 * @param factory a factory to create the instance based on the resolved attributes
	 * @param <T> the type of the instance
	 * @return a new instance
	 */
	default <T> T create(GenericApplicationContext context, SmartFunction<InjectedElementAttributes, T> factory) {
		InjectedElementAttributes attributes = resolve(context);
		return factory.apply(attributes);
	}

}
