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
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.MethodParameter;

/**
 * An {@link InjectedElementResolver} for a {@link Method}.
 *
 * @author Stephane Nicoll
 */
class InjectedMethodResolver implements InjectedElementResolver {

	private final Method method;

	private final Class<?> target;

	private final String beanName;

	/**
	 * Create a new instance.
	 * @param method the method to handle
	 * @param target the type on which the method is declared
	 * @param beanName the name of the bean, or {@code null}
	 */
	InjectedMethodResolver(Method method, Class<?> target, String beanName) {
		this.method = method;
		this.target = target;
		this.beanName = beanName;
	}

	@Override
	public InjectedElementAttributes resolve(DefaultListableBeanFactory beanFactory, boolean required) {
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

