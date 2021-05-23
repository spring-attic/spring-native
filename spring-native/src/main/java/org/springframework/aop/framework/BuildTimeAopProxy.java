/*
 * Copyright 2020-2021 the original author or authors.
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

package org.springframework.aop.framework;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.AopInvocationException;
import org.springframework.aop.RawTargetAccess;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Represent an AOP proxy that is expected to be loadable from disk. If it cannot be loaded
 * a message is constructed indicating the hint the user should provide in the application
 * to generate it at build time.
 * 
 * @author Rafael Winterhalter
 * @author Andy Clement
 */
@SuppressWarnings("serial")
public class BuildTimeAopProxy implements AopProxy, Serializable {

	protected static final Log logger = LogFactory.getLog(BuildTimeAopProxy.class);

	/**
	 * A cache that is used for avoiding repeated proxy creation.
	 */
	private static final Map<Object, Class<?>> cache = new ConcurrentHashMap<>();

	/**
	 * Keeps track of the Classes that we have validated for final methods.
	 */
	private static final Map<Class<?>, Boolean> validatedClasses = new WeakHashMap<>();

	/**
	 * The object used to configure this proxy.
	 */
	protected final AdvisedSupport advised;

	@Nullable
	protected Object[] constructorArgs;

	@Nullable
	protected Class<?>[] constructorArgTypes;

	/**
	 * Create a new BuildTimeAopProxy for the given AOP configuration.
	 *
	 * @param config the AOP configuration as AdvisedSupport object
	 * @throws AopConfigException if the config is invalid. We try to throw an informative
	 *                            exception in this case, rather than let a mysterious failure happen later.
	 */
	public BuildTimeAopProxy(AdvisedSupport config) throws AopConfigException {
		Assert.notNull(config, "AdvisedSupport must not be null");
		if (config.getAdvisors().length == 0 && config.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE) {
			throw new AopConfigException("No advisors and no TargetSource specified");
		}
		this.advised = config;
	}

	/**
	 * Set constructor arguments to use for creating the proxy.
	 *
	 * @param constructorArgs     the constructor argument values
	 * @param constructorArgTypes the constructor argument types
	 */
	public void setConstructorArguments(@Nullable Object[] constructorArgs, @Nullable Class<?>[] constructorArgTypes) {
		if (constructorArgs == null || constructorArgTypes == null) {
			throw new IllegalArgumentException("Both 'constructorArgs' and 'constructorArgTypes' need to be specified");
		}
		if (constructorArgs.length != constructorArgTypes.length) {
			throw new IllegalArgumentException("Number of 'constructorArgs' (" + constructorArgs.length +
					") must match number of 'constructorArgTypes' (" + constructorArgTypes.length + ")");
		}
		this.constructorArgs = constructorArgs;
		this.constructorArgTypes = constructorArgTypes;
	}

	@Override
	public Object getProxy() {
		return getProxy(null);
	}

	@Override
	public Object getProxy(@Nullable ClassLoader classLoader) {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating Build Time Proxy: target source is " + this.advised.getTargetSource());
		}
		try {
			Class<?> rootClass = this.advised.getTargetClass();
			Assert.state(rootClass != null, "Target class must be available for creating a Build Time Proxy");

			Class<?> proxySuperClass = rootClass;
			if (rootClass.getName().contains(ClassUtils.CGLIB_CLASS_SEPARATOR)) {
				proxySuperClass = rootClass.getSuperclass();
				Class<?>[] additionalInterfaces = rootClass.getInterfaces();
				for (Class<?> additionalInterface : additionalInterfaces) {
					if (additionalInterface != _AdvisedSupportAware.class) {
						this.advised.addInterface(additionalInterface);
					}
				}
			}

			validateClassIfNecessary(proxySuperClass, classLoader);

			ClassLoader targetClassLoader;
			if (classLoader == null) {
				targetClassLoader = proxySuperClass.getClassLoader();
				if (targetClassLoader == null) {
					targetClassLoader = getClass().getClassLoader();
				}
			} else {
				targetClassLoader = classLoader;
			}

			ProxyConfiguration configuration = ProxyConfiguration.get(advised, targetClassLoader);
			Class<?> proxyType = cache.get(configuration);
			if (proxyType == null) {
				synchronized (cache) {
					proxyType = cache.get(configuration);
					if (proxyType == null) {
						proxyType = attemptToLoadProxyClass(configuration, targetClassLoader);
						if (proxyType == null) {
							throw new IllegalStateException("Class proxy missing at runtime, hint required at build time: "+
									configuration.asHint());
						}
						cache.put(configuration, proxyType);
					}
				}

			}
			Object proxy = createProxyInstance(proxyType);
			((_AdvisedSupportAware) proxy)._setAdvised(this.advised);
			return proxy;
		} catch (IllegalStateException ex) {
			throw new AopConfigException("Unexpected problem loading and instantiating proxy for target class "+
					advised.getTargetClass() , ex);
		}
		catch (Exception ex) {
			throw new AopConfigException("Unexpected AOP exception", ex);
		}
	}

	private static Class<?> attemptToLoadProxyClass(ProxyConfiguration configuration, ClassLoader classLoader) {
		logger.info("Attempting discovery (load) of build time generated proxy for class: "+configuration.getTargetClass());
		String proxyClassName = configuration.getProxyClassName();
		try {
			Class<?> proxyClass = ClassUtils.resolveClassName(proxyClassName, classLoader);
			logger.info("Suitable proxy found with name "+proxyClassName);
			return proxyClass;
		} catch (Throwable t) {
			logger.info("No suitable proxy found with name "+proxyClassName);
		}
		return null;
	}
	
	protected Object createProxyInstance(Class<?> proxyClass) throws Exception {
		return this.constructorArgs != null ?
				proxyClass.getDeclaredConstructor(this.constructorArgTypes).newInstance(this.constructorArgs) :
				proxyClass.getDeclaredConstructor().newInstance();
	}

	/**
	 * Checks to see whether the supplied {@code Class} has already been validated and
	 * validates it if not.
	 */
	private void validateClassIfNecessary(Class<?> proxySuperClass, @Nullable ClassLoader proxyClassLoader) {
		if (logger.isInfoEnabled()) {
			synchronized (validatedClasses) {
				if (!validatedClasses.containsKey(proxySuperClass)) {
					doValidateClass(proxySuperClass, proxyClassLoader);
					validatedClasses.put(proxySuperClass, Boolean.TRUE);
				}
			}
		}
	}

	/**
	 * Checks for final methods on the given {@code Class}, as well as package-visible
	 * methods across ClassLoaders, and writes warnings to the log for each one found.
	 */
	private void doValidateClass(Class<?> proxySuperClass, @Nullable ClassLoader proxyClassLoader) {
		if (Object.class != proxySuperClass) {
			Method[] methods = proxySuperClass.getDeclaredMethods();
			for (Method method : methods) {
				int mod = method.getModifiers();
				if (!Modifier.isStatic(mod)) {
					if (Modifier.isFinal(mod)) {
						logger.info("Unable to proxy method [" + method + "] because it is final: " +
								"All calls to this method via a proxy will NOT be routed to the target instance.");
					}
					else if (!Modifier.isPublic(mod) && !Modifier.isProtected(mod) && !Modifier.isPrivate(mod) &&
							proxyClassLoader != null && proxySuperClass.getClassLoader() != proxyClassLoader) {
						logger.info("Unable to proxy method [" + method + "] because it is package-visible " +
								"across different ClassLoaders: All calls to this method via a proxy will " +
								"NOT be routed to the target instance.");
					}
				}
			}
			doValidateClass(proxySuperClass.getSuperclass(), proxyClassLoader);
		}
	}

	/**
	 * Process a return value. Wraps a return of {@code this} if necessary to be the
	 * {@code proxy} and also verifies that {@code null} is not returned as a primitive.
	 */
	@Nullable
	private static Object processReturnType(
			Object proxy, @Nullable Object target, Method method, @Nullable Object returnValue) {
		// Massage return value if necessary
		if (returnValue != null && returnValue == target &&
				!RawTargetAccess.class.isAssignableFrom(method.getDeclaringClass())) {
			// Special case: it returned "this". Note that we can't help
			// if the target sets a reference to itself in another returned object.
			returnValue = proxy;
		}
		Class<?> returnType = method.getReturnType();
		if (returnValue == null && returnType != Void.TYPE && returnType.isPrimitive()) {
			throw new AopInvocationException(
					"Null return value from advice does not match primitive return type for: " + method);
		}
		return returnValue;
	}

	@Override
	public boolean equals(Object other) {
		return (this == other || (other instanceof BuildTimeAopProxy &&
				AopProxyUtils.equalsInProxy(this.advised, ((BuildTimeAopProxy) other).advised)));
	}

	@Override
	public int hashCode() {
		return BuildTimeAopProxy.class.hashCode() * 13 + this.advised.getTargetSource().hashCode();
	}

}
