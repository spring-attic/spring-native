/*
 * Copyright 2019-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.aot.beans.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Helper class to register bean definitions in a bean factory.
 *
 * @author Stephane Nicoll
 */
public class BeanDefinitionRegistrar {

	private static final Log logger = LogFactory.getLog(BeanDefinitionRegistrar.class);

	private final String beanName;

	private final Class<?> beanClass;

	private final ResolvableType beanType;

	private final BeanDefinitionBuilder builder;

	private final List<Consumer<RootBeanDefinition>> customizers;

	private Executable instanceCreator;

	private RootBeanDefinition beanDefinition;

	/**
	 * Create a new instance.
	 * @param beanName the name of the bean
	 * @param beanClass the type of the bean
	 * @param beanType the target type with generic information (can be {@code null})
	 */
	private BeanDefinitionRegistrar(String beanName, Class<?> beanClass, ResolvableType beanType) {
		this.beanName = beanName;
		this.beanClass = beanClass;
		this.beanType = beanType;
		this.builder = BeanDefinitionBuilder.rootBeanDefinition(beanClass);
		this.customizers = new ArrayList<>();
	}

	public static BeanDefinitionRegistrar of(String beanName, ResolvableType beanType) {
		return new BeanDefinitionRegistrar(beanName, beanType.toClass(), beanType);
	}

	public static BeanDefinitionRegistrar of(String beanName, Class<?> beanType) {
		return new BeanDefinitionRegistrar(beanName, beanType, null);
	}

	public static BeanDefinitionRegistrar inner(ResolvableType beanType) {
		return of(null, beanType);
	}

	public static BeanDefinitionRegistrar inner(Class<?> beanType) {
		return of(null, beanType);
	}

	public BeanDefinitionRegistrar customize(ThrowableConsumer<RootBeanDefinition> beanDefinition) {
		this.customizers.add(beanDefinition);
		return this;
	}

	public BeanDefinitionRegistrar withFactoryMethod(Class<?> declaredType, String name, Class<?>... parameterTypes) {
		this.instanceCreator = getMethod(declaredType, name, parameterTypes);
		return this;
	}

	public BeanDefinitionRegistrar withConstructor(Class<?>... parameterTypes) {
		this.instanceCreator = getConstructor(this.beanClass, parameterTypes);
		return this;
	}

	public BeanDefinitionRegistrar instanceSupplier(ThrowableFunction<BeanInstanceContext, ?> instanceContext) {
		return customize((beanDefinition) -> beanDefinition.setInstanceSupplier(() ->
				instanceContext.apply(createBeanInstanceContext())));
	}

	public BeanDefinitionRegistrar instanceSupplier(ThrowableSupplier<?> instanceSupplier) {
		return customize((beanDefinition) -> beanDefinition.setInstanceSupplier(instanceSupplier));
	}

	public void register(DefaultListableBeanFactory beanFactory) {
		if (logger.isDebugEnabled()) {
			logger.debug("Register bean definition with name '" + this.beanName + "'");
		}
		BeanDefinition beanDefinition = toBeanDefinition();
		if (this.beanName == null) {
			throw new IllegalStateException("Bean name not set. Could not register " + beanDefinition);
		}
		beanFactory.registerBeanDefinition(this.beanName, beanDefinition);
	}

	public RootBeanDefinition toBeanDefinition() {
		try {
			this.beanDefinition = createBeanDefinition();
			return this.beanDefinition;
		}
		catch (Exception ex) {
			throw new FatalBeanException("Failed to create bean definition for bean with name '" + this.beanName + "'", ex);
		}
	}

	private RootBeanDefinition createBeanDefinition() {
		RootBeanDefinition bd = (RootBeanDefinition) builder.getBeanDefinition();
		if (this.beanType != null) {
			bd.setTargetType(this.beanType);
		}
		if (this.instanceCreator instanceof Method) {
			bd.setResolvedFactoryMethod((Method) this.instanceCreator);
		}
		this.customizers.forEach((customizer) -> customizer.accept(bd));
		return bd;
	}

	private BeanInstanceContext createBeanInstanceContext() {
		String resolvedBeanName = this.beanName != null ? this.beanName : createInnerBeanName();
		return new BeanInstanceContext(resolvedBeanName, beanClass);
	}

	private String createInnerBeanName() {
		return "(inner bean)" + BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR +
				ObjectUtils.getIdentityHexString(this.beanDefinition);
	}

	private BeanDefinition resolveBeanDefinition(DefaultListableBeanFactory beanFactory) {
		return this.beanDefinition;
	}

	private static Constructor<?> getConstructor(Class<?> beanType, Class<?>... parameterTypes) {
		try {
			return beanType.getDeclaredConstructor(parameterTypes);
		}
		catch (NoSuchMethodException ex) {
			String message = String.format("No constructor with type(s) [%s] found on %s",
					toCommaSeparatedNames(parameterTypes), beanType.getName());
			throw new IllegalArgumentException(message, ex);
		}
	}

	private static Method getMethod(Class<?> declaredType, String methodName, Class<?>... parameterTypes) {
		Method method = ReflectionUtils.findMethod(declaredType, methodName, parameterTypes);
		if (method == null) {
			String message = String.format("No method '%s' with type(s) [%s] found on %s", methodName,
					toCommaSeparatedNames(parameterTypes), declaredType.getName());
			throw new IllegalArgumentException(message);
		}
		return AopUtils.selectInvocableMethod(method, declaredType);
	}

	private static String toCommaSeparatedNames(Class<?>... parameterTypes) {
		return Arrays.stream(parameterTypes).map(Class::getName).collect(Collectors.joining(", "));
	}

	public class BeanInstanceContext {

		private final String beanName;

		private final Class<?> beanType;

		/**
		 * Create a new instance for the specified bean.
		 * @param beanName the name of the bean
		 * @param beanType the type of the bean
		 */
		private BeanInstanceContext(String beanName, Class<?> beanType) {
			this.beanName = beanName;
			this.beanType = beanType;
		}

		public <T> T create(DefaultListableBeanFactory beanFactory, ThrowableFunction<InjectedElementAttributes, T> factory) {
			return resolveInstanceCreator(BeanDefinitionRegistrar.this.instanceCreator).create(beanFactory, factory);
		}

		private InjectedElementResolver resolveInstanceCreator(Executable instanceCreator) {
			if (instanceCreator instanceof Method) {
				return new InjectedConstructionResolver(instanceCreator, instanceCreator.getDeclaringClass(), this.beanName,
						BeanDefinitionRegistrar.this::resolveBeanDefinition);
			}
			if (instanceCreator instanceof Constructor) {
				return new InjectedConstructionResolver(instanceCreator, this.beanType, this.beanName,
						BeanDefinitionRegistrar.this::resolveBeanDefinition);
			}
			throw new IllegalStateException("No factory method or constructor is set");
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
		 * Create an {@link InjectedElementResolver} for the specified bean method.
		 * @param name the name of the method on the target bean
		 * @param parameterTypes the method parameter types
		 * @return a resolved for the specified bean method
		 */
		public InjectedElementResolver method(String name, Class<?>... parameterTypes) {
			return new InjectedMethodResolver(getMethod(this.beanType, name, parameterTypes), this.beanType, this.beanName);
		}

		private Field getField(String fieldName, Class<?> fieldType) {
			Field field = ReflectionUtils.findField(this.beanType, fieldName, fieldType);
			if (field == null) {
				throw new IllegalArgumentException("No field '" + fieldName + "' with type " + fieldType.getName() + " found on " + this.beanType);
			}
			return field;
		}

	}

	/**
	 * A {@link Consumer} that allows to invoke code that throws a checked exception.
	 *
	 * @author Stephane Nicoll
	 */
	@FunctionalInterface
	public interface ThrowableConsumer<T> extends Consumer<T> {

		void acceptWithException(T t) throws Exception;

		@Override
		default void accept(T t) {
			try {
				acceptWithException(t);
			}
			catch (RuntimeException ex) {
				throw ex;
			}
			catch (Exception ex) {
				throw new RuntimeException(ex.getMessage(), ex);
			}
		}

	}

	/**
	 * A {@link Function} that allows to invoke code that throws a checked exception.
	 *
	 * @author Stephane Nicoll
	 */
	@FunctionalInterface
	public interface ThrowableFunction<T, R> extends Function<T, R> {

		R applyWithException(T t) throws Exception;

		@Override
		default R apply(T t) {
			try {
				return applyWithException(t);
			}
			catch (RuntimeException ex) {
				throw ex;
			}
			catch (Exception ex) {
				throw new RuntimeException(ex.getMessage(), ex);
			}
		}
	}

	/**
	 * A {@link Supplier} that allows to invoke code that throws a checked exception.
	 *
	 * @author Stephane Nicoll
	 */
	public interface ThrowableSupplier<T> extends Supplier<T> {

		T getWithException() throws Exception;

		@Override
		default T get() {
			try {
				return getWithException();
			}
			catch (RuntimeException ex) {
				throw ex;
			}
			catch (Exception ex) {
				throw new RuntimeException(ex.getMessage(), ex);
			}
		}
	}
}
