package org.springframework.context.bootstrap.generator.infrastructure.nativex;

import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;

/**
 * Process a {@link BeanInstanceDescriptor bean instance} and register the need for
 * native configuration. Implementation of this interface can also implement
 * {@link BeanFactoryAware} if they need to access the underlying bean factory.
 *
 * @author Stephane Nicoll
 */
@FunctionalInterface
public interface BeanNativeConfigurationProcessor {

	/**
	 * Process the specified bean and register the need for native configuration.
	 * @param descriptor the bean instance descriptor
	 * @param registry the registry to use
	 */
	void process(BeanInstanceDescriptor descriptor, NativeConfigurationRegistry registry);

}
