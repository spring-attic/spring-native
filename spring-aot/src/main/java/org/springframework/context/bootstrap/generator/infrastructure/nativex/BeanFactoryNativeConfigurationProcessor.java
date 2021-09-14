package org.springframework.context.bootstrap.generator.infrastructure.nativex;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Process the {@link ConfigurableListableBeanFactory bean factory} and register the
 * necessary native configuration.
 *
 * @author Stephane Nicoll
 * @see BeanNativeConfigurationProcessor to process a specific bean instead
 */
@FunctionalInterface
public interface BeanFactoryNativeConfigurationProcessor {

	/**
	 * Process the specified bean factory and register the need for native configuration.
	 * @param beanFactory the context to process
	 * @param registry the registry to use
	 */
	void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry);

}
