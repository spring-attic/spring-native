package org.springframework.aot.beans.factory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.MethodParameter;

/**
 * An {@link InjectedElementResolver} for a {@link Constructor}.
 *
 * @author Stephane Nicoll
 */
class InjectedConstructorResolver implements InjectedElementResolver {

	private final String beanName;

	private final Class<?> beanType;

	private final Constructor<?> constructor;

	InjectedConstructorResolver(String beanName, Class<?> beanType, Constructor<?> constructor) {
		this.beanName = beanName;
		this.beanType = beanType;
		this.constructor = constructor;
	}

	@Override
	public InjectedElementAttributes resolve(GenericApplicationContext context, boolean required) {
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		int argumentCount = this.constructor.getParameterCount();
		List<Object> arguments = new ArrayList<>();
		Set<String> autowiredBeans = new LinkedHashSet<>(argumentCount);
		TypeConverter typeConverter = beanFactory.getTypeConverter();
		for (int i = 0; i < argumentCount; i++) {
			MethodParameter methodParam = new MethodParameter(this.constructor, i);
			DependencyDescriptor depDescriptor = new DependencyDescriptor(methodParam, true);
			depDescriptor.setContainingClass(this.beanType);
			try {
				Object arg = beanFactory.resolveDependency(depDescriptor, beanName, autowiredBeans, typeConverter);
				arguments.add(arg);
			}
			catch (BeansException ex) {
				throw new UnsatisfiedDependencyException(null, beanName, new InjectionPoint(methodParam), ex);
			}
		}
		return new InjectedElementAttributes(arguments);
	}

}
