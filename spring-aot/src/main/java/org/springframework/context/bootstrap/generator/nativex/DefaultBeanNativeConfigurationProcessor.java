package org.springframework.context.bootstrap.generator.nativex;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

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
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeReflectionEntry.Builder;

/**
 * Register the reflection entries for each {@link BeanInstanceDescriptor}.
 *
 * @author Stephane Nicoll
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
		Builder builder = registry.reflection().forType(descriptor.getUserBeanClass());
		MemberDescriptor<Executable> instanceCreator = descriptor.getInstanceCreator();
		if (instanceCreator != null) {
			registry.reflection().addExecutable(instanceCreator.getMember());
		}
		for (MemberDescriptor<?> injectionPoint : descriptor.getInjectionPoints()) {
			Member member = injectionPoint.getMember();
			if (member instanceof Executable) {
				builder.withMethods((Method) member);
			}
			else if (member instanceof Field) {
				builder.withFields((Field) member);
			}
		}
		for (PropertyDescriptor property : descriptor.getProperties()) {
			Method writeMethod = property.getWriteMethod();
			if (writeMethod != null) {
				builder.withMethods(writeMethod);
			}
			Object value = property.getPropertyValue().getValue();
			if (value instanceof BeanDefinition) {
				processInnerBeanDefinition((BeanDefinition) value, registry);
			}
		}
	}

	private void processInnerBeanDefinition(BeanDefinition beanDefinition, NativeConfigurationRegistry registry) {
		if (this.beanInstanceDescriptorFactory != null) {
			process(this.beanInstanceDescriptorFactory.create(beanDefinition), registry);
		}
	}

}
