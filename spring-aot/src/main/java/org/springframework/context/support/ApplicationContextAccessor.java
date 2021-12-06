package org.springframework.context.support;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Accessor to privileged methods of {@link GenericApplicationContext}.
 *
 * @author Stephane Nicoll
 */
public class ApplicationContextAccessor {

	/**
	 * Prepare the specified {@link GenericApplicationContext} up to a point where it is
	 * ready to create bean instances
	 * @param context the context to prepare
	 * @return the processed bean factory
	 */
	public static ConfigurableListableBeanFactory prepareContext(GenericApplicationContext context) {
		context.prepareRefresh();
		ConfigurableListableBeanFactory beanFactory = context.obtainFreshBeanFactory();
		context.prepareBeanFactory(beanFactory);
		context.postProcessBeanFactory(beanFactory);
		context.invokeBeanFactoryPostProcessors(beanFactory);
		return beanFactory;
	}

}
