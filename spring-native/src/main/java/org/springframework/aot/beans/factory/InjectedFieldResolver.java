package org.springframework.aot.beans.factory;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.context.support.GenericApplicationContext;

/**
 * An {@link InjectedElementResolver} for a {@link Field}.
 *
 * @author Stephane Nicoll
 */
class InjectedFieldResolver implements InjectedElementResolver {

	private final Field field;

	private final String beanName;

	/**
	 * Create a new instance.
	 * @param field the field to handle
	 * @param beanName the name of the bean, or {@code null}
	 */
	InjectedFieldResolver(Field field, String beanName) {
		this.field = field;
		this.beanName = beanName;
	}

	@Override
	public InjectedElementAttributes resolve(GenericApplicationContext context, boolean required) {
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		DependencyDescriptor desc = new DependencyDescriptor(this.field, required);
		desc.setContainingClass(this.field.getType());
		Set<String> autowiredBeanNames = new LinkedHashSet<>(1);
		TypeConverter typeConverter = beanFactory.getTypeConverter();
		try {
			Object value = beanFactory.resolveDependency(desc, beanName, autowiredBeanNames, typeConverter);
			if (value == null && !required) {
				return new InjectedElementAttributes(null);
			}
			return new InjectedElementAttributes(Collections.singletonList(value));
		}
		catch (BeansException ex) {
			throw new UnsatisfiedDependencyException(null, this.beanName, new InjectionPoint(this.field), ex);
		}
	}

}
