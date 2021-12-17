package org.springframework.aot.beans.factory;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * Resolve the attributes of an injected element such as a {@code Constructor} or a
 * factory {@code Method}.
 *
 * @author Stephane Nicoll
 */
public interface InjectedElementResolver {

	/**
	 * Resolve the attributes using the specified {@link DefaultListableBeanFactory beanFactory}.
	 * @param beanFactory the bean factory to use
	 * @return the resolved attributes
	 */
	default InjectedElementAttributes resolve(DefaultListableBeanFactory beanFactory) {
		return resolve(beanFactory, true);
	}

	/**
	 * Resolve the attributes using the specified {@link DefaultListableBeanFactory beanFactory}.
	 * @param beanFactory the bean factory to use
	 * @param required whether the injection point is mandatory
	 * @return the resolved attributes
	 */
	InjectedElementAttributes resolve(DefaultListableBeanFactory beanFactory, boolean required);

	/**
	 * Invoke the specified consumer with the resolved {@link InjectedElementAttributes
	 * attributes}.
	 * @param beanFactory the bean factory to use to resolve the attributes
	 * @param attributes a consumer of the resolved attributes
	 */
	default void invoke(DefaultListableBeanFactory beanFactory, ThrowableConsumer<InjectedElementAttributes> attributes) {
		InjectedElementAttributes elements = resolve(beanFactory);
		attributes.accept(elements);
	}

	/**
	 * Create an instance based on the resolved {@link InjectedElementAttributes
	 * attributes}.
	 * @param beanFactory the bean factory to use to resolve the attributes
	 * @param factory a factory to create the instance based on the resolved attributes
	 * @param <T> the type of the instance
	 * @return a new instance
	 */
	default <T> T create(DefaultListableBeanFactory beanFactory, ThrowableFunction<InjectedElementAttributes, T> factory) {
		InjectedElementAttributes attributes = resolve(beanFactory);
		return factory.apply(attributes);
	}

}
