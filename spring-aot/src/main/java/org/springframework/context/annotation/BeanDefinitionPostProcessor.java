package org.springframework.context.annotation;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Strategy interface to modify a {@link BeanDefinition} that has been processed at
 * build time by {@link BuildTimeBeanDefinitionsRegistrar}.
 *
 * @author Stephane Nicoll
 */
@FunctionalInterface
public interface BeanDefinitionPostProcessor {

	/**
	 * Post-process the specified merged {@link RootBeanDefinition}.
	 * @param beanName the name of the bean
	 * @param beanDefinition the merged bean definition to post process
	 * @see ConfigurableBeanFactory#getMergedBeanDefinition(String)
	 */
	void postProcessBeanDefinition(String beanName, RootBeanDefinition beanDefinition);

}
