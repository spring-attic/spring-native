package org.springframework.aop.framework;

import net.bytebuddy.implementation.bind.annotation.*;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.AopInvocationException;
import org.springframework.aop.RawTargetAccess;
import org.springframework.aop.TargetSource;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * 
 * @author Rafael Winterhalter
 * @author Andy Clement
 */
public class Interceptors {

	public static final String ADVISED = "advised";
	
	/**
	 * Method interceptor used for static targets with no advice chain. The call
	 * is passed directly back to the target. Used when the proxy needs to be
	 * exposed and it can't be determined that the method won't return
	 * {@code this}.
	 */
	public static class StaticUnadvisedInterceptor {

		@Nullable
		@RuntimeType
		public static Object intercept(@Nullable @FieldValue(ADVISED) AdvisedSupport advised,
									   @This Object proxy,
									   @Origin Method method,
									   @Pipe Function<Object, ?> forward,
									   @Nullable @SuperCall(nullIfImpossible = true) Callable<?> superCall) throws Throwable {
			if (advised == null) {
				if (superCall == null) {
					throw new AbstractMethodError();
				} else {
					return superCall.call();
				}
			}
			Object target = advised.getTargetSource().getTarget();
			Object returnValue = forward.apply(target);
			return processReturnType(proxy, target, method, returnValue);
		}
	}


	/**
	 * General purpose AOP callback. Used when the target is dynamic or when the
	 * proxy is not frozen.
	 */
	public static class DynamicAdvisedInterceptor {

		@Nullable
		@RuntimeType
		public static Object intercept(@Nullable @FieldValue(ADVISED) AdvisedSupport advised,
									   @This Object proxy,
									   @Origin Method method,
									   @AllArguments Object[] args,
									   @Nullable @SuperCall(nullIfImpossible = true) Callable<?> superCall) throws Throwable {
			if (advised == null) {
				if (superCall == null) {
					throw new AbstractMethodError();
				} else {
					return superCall.call();
				}
			}
			Object oldProxy = null;
			boolean setProxyContext = false;
			Class<?> targetClass = null;
			Object target = null;
			try {
				if (advised.exposeProxy) {
					// Make invocation available if necessary.
					oldProxy = AopContext.setCurrentProxy(proxy);
					setProxyContext = true;
				}
				// May be null. Get as late as possible to minimize the time we
				// "own" the target, in case it comes from a pool...
				target = advised.getTargetSource().getTarget();
				if (target != null) {
					targetClass = target.getClass();
				}
				List<Object> chain = advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
				Object returnValue;
				// Check whether we only have one InvokerInterceptor: that is,
				// no real advice, but just reflective invocation of the target.

				// this check should verify the containing type for the target is also public! (see InitCountingBean used by tests)
				
				// TODO [build time proxies] dig into the 'false' here - as I recall it is not always
				// sufficient just to check the method is public, the type containing the method also
				// needs to be public? Needs testcase
				if (false && chain.isEmpty() && Modifier.isPublic(method.getModifiers())) {
					// We can skip creating a MethodInvocation: just invoke the target directly.
					// Note that the final invoker must be an InvokerInterceptor, so we know
					// it does nothing but a reflective operation on the target, and no hot
					// swapping or fancy proxying.
					Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
					try {
						returnValue = method.invoke(target, argsToUse);
					}
					catch (InvocationTargetException exception) {
						throw exception.getCause();
					}
				}
				else {
					// We need to create a method invocation...
					try {
						returnValue = new OptimizedReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain).proceed();
					}
					catch (Throwable throwable) {
						if (throwable instanceof RuntimeException || throwable instanceof Error) {
							throw throwable;
						}
						for (Class<?> exceptionType : method.getExceptionTypes()) {
							if (exceptionType.isInstance(throwable)) {
								throw throwable;
							}
						}
						throw new UndeclaredThrowableException(throwable);
					}
				}
				returnValue = processReturnType(proxy, target, method, returnValue);
				return returnValue;
			}
			finally {
				if (target != null) {
					advised.getTargetSource().releaseTarget(target);
				}
				if (setProxyContext) {
					// Restore old proxy.
					AopContext.setCurrentProxy(oldProxy);
				}
			}
		}
	}

	/**
	 * Method interceptor used for static targets with no advice chain, when the
	 * proxy is to be exposed.
	 */
	public static class StaticUnadvisedExposedInterceptor {

		@Nullable
		@RuntimeType
		public static Object intercept(@Nullable @FieldValue(ADVISED) AdvisedSupport advised,
									   @This Object proxy,
									   @Origin Method method,
									   @Pipe Function<Object, ?> forward,
									   @Nullable @SuperCall(nullIfImpossible = true) Callable<?> superCall) throws Throwable {
			if (advised == null) {
				if (superCall == null) {
					throw new AbstractMethodError();
				}
				else {
					return superCall.call();
				}
			}
			Object target = advised.getTargetSource().getTarget();
			Object oldProxy = null;
			try {
				oldProxy = AopContext.setCurrentProxy(proxy);
				Object returnValue = forward.apply(target);
				return processReturnType(proxy, target, method, returnValue);
			}
			finally {
				AopContext.setCurrentProxy(oldProxy);
			}
		}
	}

	/**
	 * Interceptor used to invoke a dynamic target without creating a method
	 * invocation or evaluating an advice chain. (We know there was no advice
	 * for this method.)
	 */
	public static class DynamicUnadvisedInterceptor {

		@Nullable
		@RuntimeType
		public static Object intercept(@Nullable @FieldValue(ADVISED) AdvisedSupport advised,
									   @This Object proxy,
									   @Origin Method method,
									   @Pipe Function<Object, ?> forward,
									   @Nullable @SuperCall(nullIfImpossible = true) Callable<?> superCall) throws Throwable {
			if (advised == null) {
				if (superCall == null) {
					throw new AbstractMethodError();
				}
				else {
					return superCall.call();
				}
			}
			TargetSource targetSource = advised.getTargetSource();
			Object target = targetSource.getTarget();
			try {
				Object returnValue = forward.apply(target);
				return processReturnType(proxy, target, method, returnValue);
			}
			finally {
				targetSource.releaseTarget(target);
			}
		}
	}

	/**
	 * Interceptor for unadvised dynamic targets when the proxy needs exposing.
	 */
	public static class DynamicUnadvisedExposedInterceptor {

		@Nullable
		@RuntimeType
		public static Object intercept(@Nullable @FieldValue(ADVISED) AdvisedSupport advised,
									   @This Object proxy,
									   @Origin Method method,
									   @Pipe Function<Object, ?> forward,
									   @Nullable @SuperCall(nullIfImpossible = true) Callable<?> superCall) throws Throwable {
			if (advised == null) {
				if (superCall == null) {
					throw new AbstractMethodError();
				}
				else {
					return superCall.call();
				}
			}
			TargetSource targetSource = advised.getTargetSource();
			Object oldProxy = null;
			Object target = targetSource.getTarget();
			try {
				oldProxy = AopContext.setCurrentProxy(proxy);
				Object returnValue = forward.apply(target);
				return processReturnType(proxy, target, method, returnValue);
			}
			finally {
				AopContext.setCurrentProxy(oldProxy);
				targetSource.releaseTarget(target);
			}
		}
	}

	public static class ForwardingInterceptor {

		@Nullable
		@RuntimeType
		public static Object intercept(@Nullable @FieldValue(ADVISED) AdvisedSupport advised,
									   @Pipe Function<Object, ?> forward,
									   @Nullable @SuperCall(nullIfImpossible = true) Callable<?> superCall) throws Throwable {
			if (advised == null) {
				if (superCall == null) {
					throw new AbstractMethodError();
				}
				else {
					return superCall.call();
				}
			}
			return forward.apply(advised.getTargetSource().getTarget());
		}
	}

	/**
	 * Dispatcher for the {@code equals} method.
	 * Ensures that the method call is always handled by this class.
	 */
	public static class EqualsInterceptor {

		@Nullable
		@RuntimeType
		public static Object intercept(@Nullable @FieldValue(ADVISED) AdvisedSupport advised,
									   @This Object proxy,
									   @Nullable @Argument(0) Object other,
									   @Nullable @SuperCall(nullIfImpossible = true) Callable<?> superCall) throws Throwable {
			if (advised == null) {
				if (superCall == null) {
					throw new AbstractMethodError();
				}
				else {
					return superCall.call();
				}
			}
			if (proxy == other) {
				return true;
			}
			if (other instanceof _AdvisedSupportAware) {
				AdvisedSupport otherAdvised = ((_AdvisedSupportAware) other)._getAdvised();
				return AopProxyUtils.equalsInProxy(advised, otherAdvised);
			}
			else {
				return false;
			}
		}
	}

	/**
	 * Dispatcher for the {@code hashCode} method.
	 * Ensures that the method call is always handled by this class.
	 */
	public static class HashCodeInterceptor {

		@Nullable
		@RuntimeType
		public static Object intercept(@Nullable @FieldValue(ADVISED) AdvisedSupport advised,
									   @Nullable @SuperCall(nullIfImpossible = true) Callable<?> superCall) throws Throwable {
			if (advised == null) {
				if (superCall == null) {
					throw new AbstractMethodError();
				}
				else {
					return superCall.call();
				}
			}
			return Interceptors.class.hashCode() * 13 + advised.getTargetSource().hashCode();
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

	/**
	 * Interceptor used specifically for advised methods on a frozen, static proxy.
	 */
	public static class FixedChainStaticTargetInterceptor implements Serializable {

		private final List<Object> adviceChain;

		public FixedChainStaticTargetInterceptor(List<Object> adviceChain) {
			this.adviceChain = adviceChain;
		}

		@Nullable
		@RuntimeType
		public Object intercept(@Nullable @FieldValue(ADVISED) AdvisedSupport advised,
				@This Object proxy,
				@Origin Method method,
				@AllArguments Object[] args,
				@Nullable @SuperCall(nullIfImpossible = true) Callable<?> superCall) throws Throwable {
			if (advised == null) {
				if (superCall == null) {
					throw new AbstractMethodError();
				}
				else {
					return superCall.call();
				}
			}
			Object target = advised.getTargetSource().getTarget();
			MethodInvocation invocation = new OptimizedReflectiveMethodInvocation(proxy, target, method, args,
					advised.getTargetClass(), this.adviceChain);
			// If we get here, we need to create a MethodInvocation.
			Object returnValue = invocation.proceed();
			returnValue = processReturnType(proxy, target, method, returnValue);
			return returnValue;
		}
	}

	/**
	 * Implementation of AOP Alliance MethodInvocation used by this AOP proxy.
	 */
	private static class OptimizedReflectiveMethodInvocation extends ReflectiveMethodInvocation {

		private final boolean publicMethod;

		public OptimizedReflectiveMethodInvocation(Object proxy, Object target, Method method, Object[] arguments,
										 Class<?> targetClass, List<Object> interceptorsAndDynamicMethodMatchers) {

			super(proxy, target, method, arguments, targetClass, interceptorsAndDynamicMethodMatchers);
			this.publicMethod = Modifier.isPublic(method.getModifiers());
		}

		/**
		 * Gives a marginal performance improvement versus using reflection to
		 * invoke the target when invoking public methods.
		 */
		@Override
		protected Object invokeJoinpoint() throws Throwable {
			// TODO [build time proxies] this optimization doesn't seem to behave very well...a (tests in spring-aop fail)
//			if (this.publicMethod) {
//				try {
//					return this.method.invoke(this.target, this.arguments);
//				}
//				catch (InvocationTargetException exception) {
//					throw exception.getCause();
//				}
//			}
//			else {
				return super.invokeJoinpoint();
//			}
		}
	}

}
