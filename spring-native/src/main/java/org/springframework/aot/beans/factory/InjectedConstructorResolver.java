package org.springframework.aot.beans.factory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.BeanDefinitionValueResolverAccessor;
import org.springframework.beans.factory.support.BeanDefinitionValueResolverAccessor.ValueResolver;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.MethodParameter;

/**
 * An {@link InjectedElementResolver} for a {@link Constructor}.
 *
 * @author Stephane Nicoll
 */
class InjectedConstructorResolver implements InjectedElementResolver {

	private final Constructor<?> constructor;

	private final Class<?> beanType;

	private final String beanName;

	private final Function<GenericApplicationContext, BeanDefinition> beanDefinitionResolver;

	/**
	 * Create a new instance.
	 * @param constructor the constructor to handle
	 * @param beanType the type of the bean
	 * @param beanName the name of the bean, or {@code null}
	 * @param beanDefinitionResolver the bean definition resolver to use
	 */
	InjectedConstructorResolver(Constructor<?> constructor, Class<?> beanType, String beanName,
			Function<GenericApplicationContext, BeanDefinition> beanDefinitionResolver) {
		this.constructor = constructor;
		this.beanType = beanType;
		this.beanName = beanName;
		this.beanDefinitionResolver = beanDefinitionResolver;
	}

	@Override
	public InjectedElementAttributes resolve(GenericApplicationContext context, boolean required) {
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		int argumentCount = this.constructor.getParameterCount();
		List<Object> arguments = new ArrayList<>();
		Set<String> autowiredBeans = new LinkedHashSet<>(argumentCount);
		TypeConverter typeConverter = beanFactory.getTypeConverter();
		ConstructorArgumentValues argumentValues = resolveArgumentValues(context);
		for (int i = 0; i < argumentCount; i++) {
			MethodParameter methodParam = new MethodParameter(this.constructor, i);
			ValueHolder valueHolder = argumentValues.getIndexedArgumentValue(i, null);
			if (valueHolder != null) {
				if (valueHolder.isConverted()) {
					arguments.add(valueHolder.getConvertedValue());
				}
				else {
					Object userValue = context.getBeanFactory().getTypeConverter()
							.convertIfNecessary(valueHolder.getValue(), methodParam.getParameterType());
					arguments.add(userValue);
				}
			}
			else {
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
		}
		return new InjectedElementAttributes(arguments);
	}

	private ConstructorArgumentValues resolveArgumentValues(GenericApplicationContext context) {
		ConstructorArgumentValues resolvedValues = new ConstructorArgumentValues();
		BeanDefinition beanDefinition = this.beanDefinitionResolver.apply(context);
		if (beanDefinition == null || !beanDefinition.hasConstructorArgumentValues()) {
			return resolvedValues;
		}
		ConstructorArgumentValues argumentValues = beanDefinition.getConstructorArgumentValues();
		ValueResolver valueResolver = BeanDefinitionValueResolverAccessor.get(context, this.beanName, beanDefinition);
		for (Map.Entry<Integer, ConstructorArgumentValues.ValueHolder> entry : argumentValues.getIndexedArgumentValues().entrySet()) {
			int index = entry.getKey();
			ConstructorArgumentValues.ValueHolder valueHolder = entry.getValue();
			if (valueHolder.isConverted()) {
				resolvedValues.addIndexedArgumentValue(index, valueHolder);
			}
			else {
				Object resolvedValue =
						valueResolver.resolveValueIfNecessary("constructor argument", valueHolder.getValue());
				ConstructorArgumentValues.ValueHolder resolvedValueHolder =
						new ConstructorArgumentValues.ValueHolder(resolvedValue, valueHolder.getType(), valueHolder.getName());
				resolvedValueHolder.setSource(valueHolder);
				resolvedValues.addIndexedArgumentValue(index, resolvedValueHolder);
			}
		}
		return resolvedValues;
	}
}

