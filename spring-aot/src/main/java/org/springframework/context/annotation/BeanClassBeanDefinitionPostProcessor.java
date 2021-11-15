package org.springframework.context.annotation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Ensure that {@link RootBeanDefinition#hasBeanClass()} can be safely used by bean
 * definition processors.
 *
 * @author Stephane Nicoll
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
class BeanClassBeanDefinitionPostProcessor implements BeanDefinitionPostProcessor, BeanFactoryAware {

	private ClassLoader classLoader;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.classLoader = ((ConfigurableBeanFactory) beanFactory).getBeanClassLoader();
	}

	@Override
	public void postProcessBeanDefinition(String beanName, RootBeanDefinition beanDefinition) {
		if (!beanDefinition.hasBeanClass()) {
			try {
				beanDefinition.resolveBeanClass(this.classLoader);
			}
			catch (ClassNotFoundException ex) {
				// ignore
			}
		}
	}

}
