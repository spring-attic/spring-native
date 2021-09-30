package org.springframework.aop.scope;

import com.squareup.javapoet.CodeBlock.Builder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.bootstrap.generator.bean.BeanRegistrationWriter;
import org.springframework.context.bootstrap.generator.bean.BeanRegistrationWriterSupplier;
import org.springframework.context.bootstrap.generator.bean.DefaultBeanRegistrationWriter;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.bean.support.MultiStatement;
import org.springframework.core.annotation.Order;

/**
 * {@link BeanRegistrationWriterSupplier} for {@link ScopedProxyFactoryBean}.
 *
 * @author Stephane Nicoll
 */
@Order(0)
class ScopedProxyBeanRegistrationWriterSupplier implements BeanRegistrationWriterSupplier, BeanFactoryAware {

	private static final Log logger = LogFactory.getLog(ScopedProxyBeanRegistrationWriterSupplier.class);

	private ConfigurableBeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (ConfigurableBeanFactory) beanFactory;
	}

	@Override
	public BeanRegistrationWriter get(String beanName, BeanDefinition beanDefinition) {
		Class<?> beanType = beanDefinition.getResolvableType().toClass();
		return (beanType.equals(ScopedProxyFactoryBean.class))
				? createScopedProxyBeanRegistrationWriter(beanName, beanDefinition) : null;
	}

	private BeanRegistrationWriter createScopedProxyBeanRegistrationWriter(String beanName, BeanDefinition beanDefinition) {
		Object targetBeanName = beanDefinition.getPropertyValues().get("targetBeanName");
		BeanDefinition targetBeanDefinition = getTargetBeanDefinition(targetBeanName);
		if (targetBeanDefinition == null) {
			logger.warn("Could not handle " + ScopedProxyFactoryBean.class.getSimpleName() +
					": no target bean definition found with name " + targetBeanName);
			return null;
		}
		RootBeanDefinition processedBeanDefinition = new RootBeanDefinition((RootBeanDefinition) beanDefinition);
		processedBeanDefinition.setTargetType(targetBeanDefinition.getResolvableType());
		processedBeanDefinition.getPropertyValues().removePropertyValue("targetBeanName");
		BeanInstanceDescriptor descriptor = BeanInstanceDescriptor.of(ScopedProxyFactoryBean.class).build();
		return new DefaultBeanRegistrationWriter(beanName, processedBeanDefinition, descriptor) {
			@Override
			protected void writeInstanceSupplier(Builder code) {
				MultiStatement statements = new MultiStatement();
				statements.add("$T factory = new $T()", ScopedProxyFactoryBean.class, ScopedProxyFactoryBean.class);
				statements.add("factory.setTargetBeanName($S)", targetBeanName);
				statements.add(("factory.setBeanFactory(context.getBeanFactory())"));
				statements.add("return factory.getObject()");
				code.add(statements.toCodeBlock("() -> "));
			}
		};
	}

	private BeanDefinition getTargetBeanDefinition(Object targetBeanName) {
		if (targetBeanName instanceof String) {
			String beanName = (String) targetBeanName;
			if (this.beanFactory.containsBean(beanName)) {
				return this.beanFactory.getMergedBeanDefinition(beanName);
			}
		}
		return null;
	}

}
