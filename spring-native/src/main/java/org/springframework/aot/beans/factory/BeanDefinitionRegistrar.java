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
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Helper class to register bean definitions in the context.
 *
 * @author Stephane Nicoll
 */
public class BeanDefinitionRegistrar {

	private final String beanName;

	private final ResolvableType beanType;

	private final BeanDefinitionBuilder builder;

	private final List<Consumer<RootBeanDefinition>> customizers;

	private RootBeanDefinition beanDefinition;

	private BeanDefinitionRegistrar(String beanName, ResolvableType beanType) {
		this.beanName = beanName;
		this.beanType = beanType;
		this.builder = BeanDefinitionBuilder.rootBeanDefinition(beanType.toClass());
		this.customizers = new ArrayList<>();
	}

	public static BeanDefinitionRegistrar of(String beanName, ResolvableType beanType) {
		return new BeanDefinitionRegistrar(beanName, beanType);
	}

	public static BeanDefinitionRegistrar of(String beanName, Class<?> beanType) {
		return of(beanName, ResolvableType.forClass(beanType));
	}

	public static BeanDefinitionRegistrar inner(ResolvableType beanType) {
		return of(null, beanType);
	}

	public static BeanDefinitionRegistrar inner(Class<?> beanType) {
		return of(null, beanType);
	}

	public BeanDefinitionRegistrar customize(SmartConsumer<RootBeanDefinition> beanDefinition) {
		this.customizers.add(beanDefinition);
		return this;
	}

	public BeanDefinitionRegistrar instanceSupplier(SmartFunction<InstanceSupplierContext, ?> instanceContext) {
		return customize((beanDefinition) -> beanDefinition.setInstanceSupplier(() ->
				instanceContext.apply(createInstanceSupplierContext())));
	}

	public BeanDefinitionRegistrar instanceSupplier(SmartSupplier<?> instanceSupplier) {
		return customize((beanDefinition) -> beanDefinition.setInstanceSupplier(instanceSupplier));
	}

	public RootBeanDefinition toBeanDefinition() {
		this.beanDefinition = (RootBeanDefinition) builder.getBeanDefinition();
		this.beanDefinition.setTargetType(this.beanType);
		this.customizers.forEach((customizer) -> customizer.accept(this.beanDefinition));
		return this.beanDefinition;
	}

	public void register(GenericApplicationContext context) {
		BeanDefinition beanDefinition = toBeanDefinition();
		if (this.beanName == null) {
			throw new IllegalStateException("Bean name not set. Could not register " + beanDefinition);
		}
		context.registerBeanDefinition(this.beanName, beanDefinition);
	}

	private InstanceSupplierContext createInstanceSupplierContext() {
		String resolvedBeanName = this.beanName != null ? this.beanName : createInnerBeanName();
		return new InstanceSupplierContext(resolvedBeanName, this.beanType.toClass());
	}

	private String createInnerBeanName() {
		return "(inner bean)" + BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR +
				ObjectUtils.getIdentityHexString(this.beanDefinition);
	}

	private BeanDefinition resolveBeanDefinition(GenericApplicationContext context) {
		return this.beanDefinition;
	}

	public class InstanceSupplierContext {

		private final String beanName;

		private final Class<?> beanType;

		/**
		 * Create a new instance for the specified bean.
		 * @param beanName the name of the bean
		 * @param beanType the type of the bean
		 */
		private InstanceSupplierContext(String beanName, Class<?> beanType) {
			this.beanName = beanName;
			this.beanType = beanType;
		}

		/**
		 * Create an {@link InjectedElementResolver} for the specified constructor.
		 * @param parameterTypes the constructor parameter types
		 * @return a resolved for the specified constructor
		 */
		public InjectedElementResolver constructor(Class<?>... parameterTypes) {
			return new InjectedConstructionResolver(getConstructor(parameterTypes), this.beanType, this.beanName,
					BeanDefinitionRegistrar.this::resolveBeanDefinition);
		}

		/**
		 * Create an {@link InjectedElementResolver} for the specified field.
		 * @param name the name of the field
		 * @param type the type of the field
		 * @return a resolved for the specified field
		 */
		public InjectedElementResolver field(String name, Class<?> type) {
			return new InjectedFieldResolver(getField(name, type), this.beanName);
		}

		/**
		 * Create an {@link InjectedElementResolver} for the specified factory method.
		 * @param declaredType the type on which the method is declared
		 * @param name the name of the method
		 * @param parameterTypes the method parameter types
		 * @return a resolved for the specified factory method
		 */
		public InjectedElementResolver method(Class<?> declaredType, String name, Class<?>... parameterTypes) {
			return new InjectedConstructionResolver(getMethod(declaredType, name, parameterTypes), declaredType, this.beanName,
					BeanDefinitionRegistrar.this::resolveBeanDefinition);
		}

		/**
		 * Create an {@link InjectedElementResolver} for the specified bean method.
		 * @param name the name of the method on the target bean
		 * @param parameterTypes the method parameter types
		 * @return a resolved for the specified bean method
		 */
		public InjectedElementResolver method(String name, Class<?>... parameterTypes) {
			return new InjectedMethodResolver(getMethod(this.beanType, name, parameterTypes), this.beanType, this.beanName);
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

		private Method getMethod(Class<?> declaredType, String methodName, Class<?>... parameterTypes) {
			Method method = ReflectionUtils.findMethod(declaredType, methodName, parameterTypes);
			if (method == null) {
				String message = String.format("No method '%s' with type(s) [%s] found on %s", methodName,
						toCommaSeparatedNames(parameterTypes), declaredType.getName());
				throw new IllegalArgumentException(message);
			}
			return AopUtils.selectInvocableMethod(method, declaredType);
		}

		private String toCommaSeparatedNames(Class<?>... parameterTypes) {
			return Arrays.stream(parameterTypes).map(Class::getName).collect(Collectors.joining(", "));
		}

	}
}
