package org.springframework.boot.actuate.autoconfigure.web;

import org.springframework.aot.context.bootstrap.generator.bean.BeanRegistrationWriter;
import org.springframework.aot.context.bootstrap.generator.bean.BeanRegistrationWriterSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.util.ClassUtils;

/**
 * A {@link BeanRegistrationWriterSupplier} for the Actuator management context factory.
 * <p/>
 * In regular mode, such factory creates a child context at runtime for the actuator
 * infrastructure, that can be replaced in AOT mode by what
 * {@link ManagementContextBeanRegistrationWriter} does.
 *
 * @author Stephane Nicoll
 */
@Order(0)
class ManagementContextBeanRegistrationWriterSupplier implements BeanRegistrationWriterSupplier, ApplicationContextAware {

	private static final String MANAGEMENT_CONTEXT_FACTORY_CLASS_NAME = "org.springframework.boot.actuate.autoconfigure.web.ManagementContextFactory";

	private GenericApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = (GenericApplicationContext) applicationContext;
	}

	@Override
	public BeanRegistrationWriter get(String beanName, BeanDefinition beanDefinition) {
		if (ClassUtils.isPresent(MANAGEMENT_CONTEXT_FACTORY_CLASS_NAME, this.applicationContext.getClassLoader())) {
			return new Supplier().get(this.applicationContext, beanName, beanDefinition);
		}
		return null;
	}

	private static class Supplier {

		private static final String DIFFERENT_MANAGEMENT_CONTEXT_TRIGGER_CLASS_NAME = "org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration.DifferentManagementContextConfiguration";

		public BeanRegistrationWriter get(GenericApplicationContext parent, String beanName, BeanDefinition beanDefinition) {
			Class<?> beanType = beanDefinition.getResolvableType().toClass();
			if (ManagementContextFactory.class.isAssignableFrom(beanType)
					&& hasManagementContext(parent.getBeanFactory())) {
				// Ahem, fuzzy search
				boolean reactive = beanType.getName().contains("reactive");
				return new ManagementContextBeanRegistrationWriter(parent, beanName, reactive);
			}
			return null;
		}

		/**
		 * Specify whether the specified bean factory requires a management context.
		 * @param beanFactory the bean factory of the main context
		 * @return {@code true} if a management context is required.
		 */
		private boolean hasManagementContext(ConfigurableListableBeanFactory beanFactory) {
			// We can't do much else than checking for the configuration that actually
			// triggers the child context creation in Spring Boot
			try {
				Class<?> trigger = ClassUtils.forName(DIFFERENT_MANAGEMENT_CONTEXT_TRIGGER_CLASS_NAME, beanFactory.getBeanClassLoader());
				return beanFactory.getBeanNamesForType(trigger).length == 1;
			}
			catch (Exception ex) {
				return false;
			}
		}
	}

}
