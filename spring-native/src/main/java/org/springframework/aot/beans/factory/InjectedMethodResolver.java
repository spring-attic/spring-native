package org.springframework.aot.beans.factory;

import java.lang.reflect.Method;
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
 * An {@link InjectedElementResolver} for a {@link Method}.
 *
 * @author Stephane Nicoll
 */
class InjectedMethodResolver implements InjectedElementResolver {

	private final String beanName;

	private final Class<?> target;

	private final Method method;

	InjectedMethodResolver(String beanName, Class<?> target, Method method) {
		this.beanName = beanName;
		this.target = target;
		this.method = method;
	}

	@Override
	public InjectedElementAttributes resolve(GenericApplicationContext context, boolean required) {
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		int argumentCount = this.method.getParameterCount();
		List<Object> arguments = new ArrayList<>();
		Set<String> autowiredBeans = new LinkedHashSet<>(argumentCount);
		TypeConverter typeConverter = beanFactory.getTypeConverter();
		for (int i = 0; i < argumentCount; i++) {
			MethodParameter methodParam = new MethodParameter(method, i);
			DependencyDescriptor depDescriptor = new DependencyDescriptor(methodParam, required);
			depDescriptor.setContainingClass(this.target);
			try {
				Object arg = beanFactory.resolveDependency(depDescriptor, beanName, autowiredBeans, typeConverter);
				if (arg == null && !required) {
					arguments = null;
					break;
				}
				arguments.add(arg);
			}
			catch (BeansException ex) {
				throw new UnsatisfiedDependencyException(null, beanName, new InjectionPoint(methodParam), ex);
			}
		}
		return new InjectedElementAttributes(arguments);
	}

}

