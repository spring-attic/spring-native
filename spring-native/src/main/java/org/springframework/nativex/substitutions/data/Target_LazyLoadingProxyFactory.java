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
package org.springframework.nativex.substitutions.data;

import java.io.Serializable;
import java.lang.reflect.Method;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.mongodb.core.convert.DbRefResolverCallback;
import org.springframework.data.mongodb.core.convert.LazyLoadingProxy;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.nativex.substitutions.OnlyIfPresent;

/**
 * @author Christoph Strobl
 * @since 0.11
 */
@TargetClass(className = "org.springframework.data.mongodb.core.convert.LazyLoadingProxyFactory", onlyWith = {OnlyIfPresent.class, OnlyIfImperativeMongoClient.class})
public final class Target_LazyLoadingProxyFactory {

	@Alias
	private PersistenceExceptionTranslator exceptionTranslator = null;

	@Substitute
	public Object createLazyLoadingProxy(MongoPersistentProperty property, DbRefResolverCallback callback,
			Object source) {

		Class<?> propertyType = property.getType();
		LazyLoadingInterceptor interceptor = new LazyLoadingInterceptor(property, callback, source, exceptionTranslator);

		if (!propertyType.isInterface()) {

			ProxyFactory factory = new ProxyFactory();
			factory.addAdvice(interceptor);
			factory.addInterface(LazyLoadingProxy.class);
			factory.setTargetClass(propertyType);
			factory.setProxyTargetClass(true);
			return factory.getProxy(propertyType.getClassLoader());
		}

		ProxyFactory proxyFactory = new ProxyFactory();

		for (Class<?> type : propertyType.getInterfaces()) {
			proxyFactory.addInterface(type);
		}

		proxyFactory.addInterface(LazyLoadingProxy.class);
		proxyFactory.addInterface(propertyType);
		proxyFactory.addAdvice(interceptor);

		return proxyFactory.getProxy(LazyLoadingProxy.class.getClassLoader());
	}

	@TargetClass(className = "org.springframework.data.mongodb.core.convert.LazyLoadingProxyFactory", innerClass = "LazyLoadingInterceptor", onlyWith = {OnlyIfPresent.class, OnlyIfImperativeMongoClient.class})
	static final class LazyLoadingInterceptor
			implements MethodInterceptor, org.springframework.cglib.proxy.MethodInterceptor, Serializable {

		@Alias
		public LazyLoadingInterceptor(MongoPersistentProperty property, DbRefResolverCallback callback, Object source,
				PersistenceExceptionTranslator exceptionTranslator) {
		}

		@Alias
		public Object invoke(MethodInvocation invocation) throws Throwable {
			return null;
		}

		@Alias
		public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
			return null;
		}
	}
}
