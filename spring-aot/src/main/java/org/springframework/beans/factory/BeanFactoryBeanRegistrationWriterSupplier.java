package org.springframework.beans.factory;

import java.util.function.Predicate;

import org.springframework.aot.context.bootstrap.generator.bean.AbstractBeanRegistrationWriterSupplier;
import org.springframework.aot.context.bootstrap.generator.bean.BeanRegistrationWriter;
import org.springframework.aot.context.bootstrap.generator.bean.BeanRegistrationWriterSupplier;
import org.springframework.aot.context.bootstrap.generator.bean.DefaultBeanRegistrationWriter;
import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;

/**
 * A {@link BeanRegistrationWriterSupplier} for {@link FactoryBean factory beans} that
 * define the target type using the {@value FactoryBean#OBJECT_TYPE_ATTRIBUTE}.
 *
 * @author Stephane Nicoll
 */
@Order(0)
class BeanFactoryBeanRegistrationWriterSupplier extends AbstractBeanRegistrationWriterSupplier {

	@Override
	protected boolean isCandidate(String beanName, BeanDefinition beanDefinition) {
		Class<?> factoryBeanType = beanDefinition.getResolvableType().toClass();
		if (FactoryBean.class.isAssignableFrom(factoryBeanType)) {
			ResolvableType targetType = determineTargetType(beanDefinition);
			return (targetType != null && isCompatible(factoryBeanType, targetType));
		}
		return false;
	}

	@Override
	protected BeanRegistrationWriter createInstance(String beanName, BeanDefinition beanDefinition,
			BeanInstanceDescriptor beanInstanceDescriptor) {
		return new DefaultBeanRegistrationWriter(beanName, beanDefinition, beanInstanceDescriptor) {
			@Override
			protected Predicate<String> getAttributeFilter() {
				return super.getAttributeFilter().or((candidate) ->
						candidate.equals(FactoryBean.OBJECT_TYPE_ATTRIBUTE));
			}
		};
	}

	@Nullable
	private ResolvableType determineTargetType(BeanDefinition beanDefinition) {
		Object objectTypeAttribute = beanDefinition.getAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE);
		if (objectTypeAttribute instanceof ResolvableType) {
			return (ResolvableType) objectTypeAttribute;
		}
		if (objectTypeAttribute instanceof Class<?>) {
			return ResolvableType.forClass((Class<?>) objectTypeAttribute);
		}
		return null;
	}

	private boolean isCompatible(Class<?> factoryBeanClass, ResolvableType candidate) {
		return ResolvableType.forClass(factoryBeanClass).as(FactoryBean.class)
				.getGeneric(0).isAssignableFrom(candidate);
	}

}
