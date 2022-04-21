/*
 * Copyright 2021 the original author or authors.
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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

import net.bytebuddy.ClassFileVersion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.auxiliary.AuxiliaryType;
import net.bytebuddy.implementation.bind.annotation.Pipe;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Proxy generator for a Spring proxy - built using Byte Buddy.
 * 
 * @author Rafael Winterhalter
 * @author Andy Clement
 */
public class ProxyGenerator {

	protected static final Log logger = LogFactory.getLog(ProxyGenerator.class);

	/**
	 * Keeps track of the Classes that we have validated for final methods.
	 */
	private static final Map<Class<?>, Boolean> validatedClasses = new WeakHashMap<>();
	
	public static DynamicType.Unloaded<?> getProxyBytes(BuildTimeProxyDescriptor cpd, ClassLoader classLoader) {
		ProxyConfiguration config = ProxyConfiguration.get(cpd, classLoader);
		logger.info("Generating class file bytes for a proxy named "+config.getProxyClassName());
		try {
			String targetClass = config.getTargetClass();

			Class<?> proxySuperClass = ClassUtils.forName(targetClass, classLoader);

			if (Modifier.isFinal(proxySuperClass.getModifiers())) {
				throw new IllegalStateException("Cannot create a build time proxy for a final class: "+targetClass);
			}
			
			validateClassIfNecessary(proxySuperClass, classLoader);

			Class<?> target = resolve(targetClass, classLoader);
			ByteBuddy byteBuddy = new ByteBuddy(ClassFileVersion.of(target)).with(TypeValidation.DISABLED);
			byteBuddy = byteBuddy.ignore(ElementMatchers.none());
			byteBuddy = byteBuddy.with(new ProxyNamingStrategy(config));
			byteBuddy = byteBuddy.with(new AuxiliaryTypeNamingStrategy());

			DynamicType.Builder<?> builder = byteBuddy.subclass(target);
			builder = builder.implement(resolve(config.getProxiedInterfaces(), classLoader));

			builder = configure(builder, proxySuperClass, config, classLoader);

			builder = builder.defineField(Interceptors.ADVISED, AdvisedSupport.class, Visibility.PRIVATE);
			builder = builder.implement(_AdvisedSupportAware.class)
					.method(ElementMatchers.named("_setAdvised")
							.or(ElementMatchers.named("_getAdvised")))
					.intercept(FieldAccessor.ofField(Interceptors.ADVISED));
			DynamicType.Unloaded<?> type = builder.make();
			return type;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new IllegalStateException("Problem creating proxy with configuration: "+config, ex);
		}
	}

	public static ProxyConfiguration getConfig(AdvisedSupport advised,ClassLoader classLoader) {
		ProxyConfiguration configuration = ProxyConfiguration.get(advised, classLoader);
		return configuration;
	}

	public static Class<?>[] resolve(List<String> types, ClassLoader classLoader) {
		Class<?>[] result = new Class<?>[types.size()];
		for (int i=0;i<types.size();i++) {
			result[i] = resolve(types.get(i),classLoader);
		}
		return result;
	}

	public static Class<?> resolve(String type, ClassLoader classLoader) {
		return ClassUtils.resolveClassName(type, classLoader);
	}

	/**
	 * Checks to see whether the supplied {@code Class} has already been validated and
	 * validates it if not.
	 */
	private static void validateClassIfNecessary(Class<?> proxySuperClass, @Nullable ClassLoader proxyClassLoader) {
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
	private static void doValidateClass(Class<?> proxySuperClass, @Nullable ClassLoader proxyClassLoader) {
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
			if(proxySuperClass.getSuperclass() != null) { // required?
				doValidateClass(proxySuperClass.getSuperclass(), proxyClassLoader);
			}
		}
	}

	/**
	 * Allows custom configuration. A custom configuration must yield the same proxy for equal
	 * input parameters.
	 *
	 * @param builder     the builder that should be used for creating the proxy
	 * @param rootClass   the root class that is being proxied
	 * @param config      the ProxyConfiguration distilled from the AdvisedSupport object
	 * @param classLoader the classloader to use
	 * @return a fully configured builder.
	 * @throws Exception if an error occurs during the configuration.
	 */
	protected static DynamicType.Builder<?> configure(
			DynamicType.Builder<?> builder,
			Class<?> rootClass,
			ProxyConfiguration config, 
			ClassLoader classLoader) throws Exception {

		Class<?> targetClass = resolve(config.getTargetClass(),classLoader);

		MethodDelegation.WithCustomProperties invokeConfiguration = MethodDelegation.withDefaultConfiguration()
				.withBinders(Pipe.Binder.install(Function.class));
		MethodDelegation invokeTarget;
		if (config.isExposeProxy()) {
			invokeTarget = invokeConfiguration.to(config.isStatic() ?
					Interceptors.StaticUnadvisedExposedInterceptor.class :
					Interceptors.DynamicUnadvisedExposedInterceptor.class);
		} else {
			invokeTarget = invokeConfiguration.to(config.isStatic() ?
					Interceptors.StaticUnadvisedInterceptor.class :
					Interceptors.DynamicUnadvisedInterceptor.class);
		}

		MethodDelegation aopProxy = MethodDelegation.to(Interceptors.DynamicAdvisedInterceptor.class);

		Implementation adviceDispatched = MethodCall.invokeSelf().onField(Interceptors.ADVISED).withAllArguments();

		Implementation dispatchTarget = config.isStatic() ?
				MethodDelegation.withDefaultConfiguration().withBinders(Pipe.Binder.install(Function.class)).to(Interceptors.ForwardingInterceptor.class) :
				SuperMethodCall.INSTANCE;

		builder = builder.ignoreAlso((ElementMatcher<MethodDescription>) target -> {
			if (ElementMatchers.isFinalizer().matches(target)) {
				logger.debug("Found finalize() method - using NO_OVERRIDE");
				return true;
			}
			return false;
		});

		builder = builder.method(target -> {
			if (logger.isDebugEnabled()) {
				logger.debug("Method " + target +
						"has return type that is assignable from the target type (may return this) - " +
						"using INVOKE_TARGET");
			}
			return true;
		}).intercept(invokeTarget);

		builder = builder.method(target -> {
			TypeDescription returnType = target.getReturnType().asErasure();
			if (returnType.isPrimitive() || !returnType.isAssignableFrom(targetClass)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Method " + target +
							" has return type that ensures this cannot be returned- using DISPATCH_TARGET");
				}
				return true;
			}
			return false;
		}).intercept(dispatchTarget);

		builder = builder.method(target -> {
			TypeDescription returnType = target.getReturnType().asErasure();
			if (returnType.represents(targetClass)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Method " + target +
							"has return type same as target type (may return this) - using INVOKE_TARGET");
				}
				return true;
			}
			return false;
		}).intercept(invokeTarget);

		// See if the return type of the method is outside the class hierarchy
		// of the target type. If so we know it never needs to have return type
		// massage and can use a dispatcher.
		// If the proxy is being exposed, then must use the interceptor the
		// correct one is already configured. If the target is not static, then
		// cannot use a dispatcher because the target cannot be released.
		builder = builder.method(target -> config.isExposeProxy() || !config.isStatic()).intercept(invokeTarget);

		// TODO [build time proxies] at build time we don't know about the advice chain - so we can' do any optimizations
		builder = builder.method(target -> {
//			Method method = ((MethodDescription.ForLoadedMethod) target.asDefined()).getLoadedMethod();
//			if (!this.config.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass).isEmpty() || !isFrozen) {
//				if (logger.isDebugEnabled()) {
//					logger.debug("Unable to apply any optimisations to advised method: " + target);
//				}
			return true;
//			}
//			else {
//				return false;
//			}
		}).intercept(aopProxy);

		// TODO [build time proxies] at build time we don't know if there is no advice - *could* make it definable via the hint but feels a little messy
//		if (isStatic && isFrozen) {
//			Method[] methods = rootClass.getMethods();
//
//			// TODO: small memory optimisation here (can skip creation for methods with no advice)
//			for (Method method : methods) {
//				List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, rootClass);
//				Implementation fixedChainStaticTargetInterceptor = MethodDelegation.to(new Interceptors.FixedChainStaticTargetInterceptor(chain));
//
//				builder = builder.method(target -> {
//					if (target.asDefined().represents(method)) {
//						if (logger.isDebugEnabled()) {
//							logger.debug("Method has advice and optimisations are enabled: " + target);
//						}
//						return true;
//					}
//					return false;
//				}).intercept(fixedChainStaticTargetInterceptor);
//			}
//		}

		builder = builder.method(target -> {
			// If exposing the proxy, then AOP_PROXY must be used.
			if (config.isExposeProxy()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Must expose proxy on advised method: " + target);
				}
				return true;
			}
			return false;
		}).intercept(aopProxy);

		builder = builder.method(target -> {
			if (ElementMatchers.isHashCode().matches(target)) {
				logger.debug("Found 'hashCode' method: " + target);
				return true;
			}
			return false;
		}).intercept(MethodDelegation.to(Interceptors.HashCodeInterceptor.class));

		builder = builder.method(target -> {
			if (ElementMatchers.isEquals().matches(target)) {
				logger.debug("Found 'equals' method: " + target);
				return true;
			}
			return false;
		}).intercept(MethodDelegation.to(Interceptors.EqualsInterceptor.class));

		builder = builder.method(target -> {
			if (!config.isOpaque() && target.getDeclaringType().isInterface() &&
					target.getDeclaringType().asErasure().isAssignableFrom(Advised.class)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Method is declared on Advised interface: " + target);
				}
				return true;
			}
			return false;
		}).intercept(adviceDispatched);

		return builder;
	}

	/**
	 * Custom name strategy for the Spring proxy - stable between build time and run
	 * time. The name is determined by the config object that takes into account
	 * the types involved (superclass, interfaces) and any conditional flags which,
	 * when they vary, would alter the shape of the generated bytecode.
	 */
	private static class ProxyNamingStrategy extends NamingStrategy.AbstractBase {

		private final ProxyConfiguration config;

		ProxyNamingStrategy(ProxyConfiguration config) {
			this.config = config;
		}

		@Override
		protected String name(TypeDescription superClass) {
			return config.getProxyClassName();
		}

	}

	/**
	 * Simple name strategy for the auxiliary types built to support fast proxy execution.
	 */
	private static class AuxiliaryTypeNamingStrategy implements AuxiliaryType.NamingStrategy {

		private int counter = 1;

		public AuxiliaryTypeNamingStrategy() {}

		@Override
		public String name(TypeDescription instrumentedType, AuxiliaryType auxiliaryType) {
			return instrumentedType.getName()+"$aux$"+Integer.toHexString(counter++);
		}
	}
}
