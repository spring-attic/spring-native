package org.springframework.context.bootstrap.generator.nativex;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.MemberDescriptor;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.PropertyDescriptor;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptorFactory;
import org.springframework.context.bootstrap.generator.bean.descriptor.DefaultBeanInstanceDescriptorFactory;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.BeanNativeConfigurationProcessor;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry.ReflectionConfiguration;

/**
 * Register the reflection entries for each {@link BeanInstanceDescriptor}.
 *
 * @author Stephane Nicoll
 * @author Christoph Strobl
 */
class DefaultBeanNativeConfigurationProcessor implements BeanNativeConfigurationProcessor, BeanFactoryAware {

	private BeanInstanceDescriptorFactory beanInstanceDescriptorFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanInstanceDescriptorFactory = new DefaultBeanInstanceDescriptorFactory(
				(ConfigurableBeanFactory) beanFactory);
	}

	@Override
	public void process(BeanInstanceDescriptor descriptor, NativeConfigurationRegistry registry) {
		ReflectionConfiguration reflectionConfiguration = registry.reflection();
		MemberDescriptor<Executable> instanceCreator = descriptor.getInstanceCreator();
		if (instanceCreator != null) {
			reflectionConfiguration.addExecutable(instanceCreator.getMember());
		}
		for (MemberDescriptor<?> injectionPoint : descriptor.getInjectionPoints()) {
			Member member = injectionPoint.getMember();
			if (member instanceof Executable) {
				reflectionConfiguration.addExecutable((Method) member);
			}
			else if (member instanceof Field) {
				reflectionConfiguration.addField((Field) member);
			}
		}
		for (PropertyDescriptor property : descriptor.getProperties()) {
			Method writeMethod = property.getWriteMethod();
			if (writeMethod != null) {
				reflectionConfiguration.addExecutable(writeMethod);
			}
			Object value = property.getPropertyValue().getValue();
			if (value instanceof BeanDefinition) {
				processInnerBeanDefinition((BeanDefinition) value, registry);
			}
		}
		/*
		 * Register required reflection configuration for private bean initialization (post construct) methods
		 * that need to be called via ReflectionUtils.
		 */
		for (MemberDescriptor<Method> callback : descriptor.getInitializationMethods()) {
			Method targetMethod = callback.getMember();
			if (Modifier.isPrivate(targetMethod.getModifiers())) {
				registry.reflection().forType(targetMethod.getDeclaringClass()).withMethods(targetMethod);
			}
		}
	}

	private void processInnerBeanDefinition(BeanDefinition beanDefinition, NativeConfigurationRegistry registry) {
		if (this.beanInstanceDescriptorFactory != null) {
			process(this.beanInstanceDescriptorFactory.create(beanDefinition), registry);
		}
	}

}