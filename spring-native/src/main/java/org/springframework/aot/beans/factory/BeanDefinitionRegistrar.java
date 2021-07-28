package org.springframework.aot.beans.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.util.ReflectionUtils;

/**
 * Helper class to register bean definitions in the context.
 *
 * @author Stephane Nicoll
 */
public class BeanDefinitionRegistrar {

	private final String beanName;

	private final ResolvableType beanType;

	private final InstanceSupplierContext context;

	private final BeanDefinitionBuilder builder;

	private final List<Consumer<RootBeanDefinition>> customizers;

	private BeanDefinitionRegistrar(String beanName, ResolvableType beanType) {
		this.beanName = beanName;
		this.beanType = beanType;
		this.context = new InstanceSupplierContext(beanName, beanType.toClass());
		this.builder = BeanDefinitionBuilder.rootBeanDefinition(beanType.toClass());
		this.customizers = new ArrayList<>();
	}

	public static BeanDefinitionRegistrar of(String beanName, ResolvableType beanType) {
		return new BeanDefinitionRegistrar(beanName, beanType);
	}

	public static BeanDefinitionRegistrar of(String beanName, Class<?> beanType) {
		return of(beanName, ResolvableType.forClass(beanType));
	}

	public BeanDefinitionRegistrar customize(Consumer<BeanDefinitionBuilder> builder) {
		builder.accept(this.builder);
		return this;
	}

	public BeanDefinitionRegistrar instanceSupplier(SmartFunction<InstanceSupplierContext, ?> instanceContext) {
		this.customizers.add((beanDefinition) -> beanDefinition.setInstanceSupplier(() -> instanceContext.apply(this.context)));
		return this;
	}

	public BeanDefinitionRegistrar instanceSupplier(SmartSupplier<?> instanceSupplier) {
		this.customizers.add((beanDefinition) -> beanDefinition.setInstanceSupplier(instanceSupplier));
		return this;
	}


	public void register(GenericApplicationContext context) {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) builder.getBeanDefinition();
		beanDefinition.setTargetType(this.beanType);
		this.customizers.forEach((customizer) -> customizer.accept(beanDefinition));
		context.registerBeanDefinition(this.beanName, beanDefinition);
	}

	public static class InstanceSupplierContext {

		private final String beanName;

		private final Class<?> beanType;

		public InstanceSupplierContext(String beanName, Class<?> beanType) {
			this.beanName = beanName;
			this.beanType = beanType;
		}

		public InjectedElementResolver constructor(Class<?>... parameterTypes) {
			return new InjectedConstructorResolver(this.beanName, this.beanType, getConstructor(parameterTypes));
		}

		public InjectedElementResolver field(String name, Class<?> type) {
			return new InjectedFieldResolver(this.beanName, getField(name, type));
		}

		public InjectedElementResolver method(String name, Class<?>... parameterTypes) {
			return new InjectedMethodResolver(this.beanName, this.beanType, getMethod(name, parameterTypes));
		}

		private Constructor<?> getConstructor(Class<?>... parameterTypes) {
			try {
				return this.beanType.getDeclaredConstructor(parameterTypes);
			}
			catch (NoSuchMethodException ex) {
				String message = String.format("No constructor with type(s) [%s] found on %s",
						toCommaSeparatedNames(parameterTypes), this.beanType.getName());
				throw new IllegalArgumentException(message, ex);
			}
		}

		private Field getField(String fieldName, Class<?> fieldType) {
			Field field = ReflectionUtils.findField(this.beanType, fieldName, fieldType);
			if (field == null) {
				throw new IllegalArgumentException("No field '" + fieldName + "' with type " + fieldType.getName() + " found on " + this.beanType);
			}
			return field;
		}

		private Method getMethod(String methodName, Class<?>... parameterTypes) {
			Method method = ReflectionUtils.findMethod(this.beanType, methodName, parameterTypes);
			if (method == null) {
				String message = String.format("No method '%s' with type(s) [%s] found on %s", methodName,
						toCommaSeparatedNames(parameterTypes), this.beanType.getName());
				throw new IllegalArgumentException(message);
			}
			return AopUtils.selectInvocableMethod(method, this.beanType);
		}

		private String toCommaSeparatedNames(Class<?>... parameterTypes) {
			return Arrays.stream(parameterTypes).map(Class::getName).collect(Collectors.joining(", "));
		}

	}
}
