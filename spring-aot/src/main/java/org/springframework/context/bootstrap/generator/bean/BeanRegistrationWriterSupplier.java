package org.springframework.context.bootstrap.generator.bean;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 * Strategy interface to provide the bean registration writer for a {@link BeanDefinition}.
 *
 * @author Stephane Nicoll
 */
@FunctionalInterface
public interface BeanRegistrationWriterSupplier {

	/**
	 * Return the {@link BeanRegistrationWriter} to use for the specified merged
	 * {@link BeanDefinition}.
	 * @param beanName the name of the bean definition to handle
	 * @param beanDefinition the merged bean definition to handle
	 * @return the {@link BeanRegistrationWriter} to use, or {@code null}
	 * @see ConfigurableBeanFactory#getMergedBeanDefinition(String)
	 */
	BeanRegistrationWriter get(String beanName, BeanDefinition beanDefinition);
}
