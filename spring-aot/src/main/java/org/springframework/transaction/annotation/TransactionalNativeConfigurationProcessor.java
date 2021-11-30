/*
 * Copyright 2019-2021 the original author or authors.
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

package org.springframework.transaction.annotation;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanFactoryNativeConfigurationProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeProxyEntry;
import org.springframework.aot.support.BeanFactoryProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.DecoratingProxy;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.nativex.hint.ProxyBits;
import org.springframework.util.ClassUtils;

/**
 * Recognize components that need transactional proxies and register them.
 *
 * @author Andy Clement
 * @author Petr Hejl
 */
public class TransactionalNativeConfigurationProcessor implements BeanFactoryNativeConfigurationProcessor {

	private static Log logger = LogFactory.getLog(TransactionalNativeConfigurationProcessor.class);

	protected final static String TRANSACTIONAL_CLASS_NAME = "org.springframework.transaction.annotation.Transactional";

	protected final static String JAVAX_TRANSACTIONAL_CLASS_NAME = "javax.transaction.Transactional";

	protected final static String SPRING_PROXY_CLASS_NAME = "org.springframework.transaction.annotation.Transactional";

	@Override
	public void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
		if ((ClassUtils.isPresent(TRANSACTIONAL_CLASS_NAME, beanFactory.getBeanClassLoader()) ||
			ClassUtils.isPresent(JAVAX_TRANSACTIONAL_CLASS_NAME, beanFactory.getBeanClassLoader())) &&
			ClassUtils.isPresent(SPRING_PROXY_CLASS_NAME, beanFactory.getBeanClassLoader())) {
			new Processor().process(beanFactory, registry);
		}
	}

	private static boolean isTransactional(Class<?> beanType) {
		return hasTransactionAnnotation(beanType, new HashSet<>());
	}
	
	/**
	 * Try and find either of the two possible transaction attributes on the type hierarchy or a method
	 * within the hierarchy.
	 */
	private static boolean hasTransactionAnnotation(Class<?> type, Set<String> visited) {
		if (!visited.add(type.getName())) {
			return false;
		}
		MergedAnnotations annos = MergedAnnotations.from(type);
		if (annos.get(TRANSACTIONAL_CLASS_NAME).isPresent() || annos.get(JAVAX_TRANSACTIONAL_CLASS_NAME).isPresent()) {
			return true;
		}
		for (Method method: type.getDeclaredMethods()) {
			annos = MergedAnnotations.from(method);
			if (annos.get(TRANSACTIONAL_CLASS_NAME).isPresent() || annos.get(JAVAX_TRANSACTIONAL_CLASS_NAME).isPresent()) {
				return true;
			}			
		}
		Class<?> superClass = type.getSuperclass();
		if (superClass != null) {
			boolean found = hasTransactionAnnotation(superClass, visited);
			if (found) {
				return true;
			}
		}
		for (Class<?> intface: type.getInterfaces()) {
			boolean found = hasTransactionAnnotation(intface, visited);
			if (found) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean hasInterfaceMethods(Class<?> type) {
		return hasInterfaceMethods(type, new HashSet<>());
	}
	
	
	/**
	 * Check if any interfaces in this types hierarchy have methods in them.
	 * 
	 * @param type the type to check
	 * @param visited the types already checked whilst doing a searching visit
	 * @return true if the type or any of its supertypes are an interface with at least one method
	 */
	private static boolean hasInterfaceMethods(Class<?> type, Set<String> visited) {
		if (!visited.add(type.getName())) {
			return false;
		}
		if (type.isInterface() && type.getMethods().length!=0) {
			return true;
		}
		for (Class<?> intface: type.getInterfaces()) {
			boolean found = hasInterfaceMethods(intface, visited);
			if (found) {
				return true;
			}
		}
		Class<?> superClass = type.getSuperclass();
		if (superClass != null) {
			boolean found = hasInterfaceMethods(superClass, visited);
			if (found) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Find and collect the list of interfaces implemented by a specified class. It does *not*
	 * recurse into interfaces.
	 */
	public static void collectInterfaces(Class<?> clazz, LinkedHashSet<String> collector) {
		for (Class<?> intface: clazz.getInterfaces()) {
			collector.add(intface.getName());
		}
		Class<?> superclass = clazz.getSuperclass();
		if (superclass != null) {
			collectInterfaces(superclass, collector);
		}
	}

	private static class Processor {

		void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
			new BeanFactoryProcessor(beanFactory).processBeans(TransactionalNativeConfigurationProcessor::isTransactional,
				(beanName, beanType) -> {
					if (hasInterfaceMethods(beanType)) {
					    LinkedHashSet<String> interfaces = new LinkedHashSet<>();
						collectInterfaces(beanType, interfaces);
						// jdbc-tx sample requires this kind of JDK Proxy
						//  [interface org.springframework.boot.CommandLineRunner, 
						//   interface app.main.Finder, 
						//   interface org.springframework.aop.SpringProxy, 
						//   interface org.springframework.aop.framework.Advised, 
						//   interface org.springframework.core.DecoratingProxy]
						if (interfaces.size()!=0) {
							interfaces.add(SpringProxy.class.getName());
							interfaces.add(Advised.class.getName());//"org.springframework.aop.framework.Advised");
							interfaces.add(DecoratingProxy.class.getName());//"org.springframework.core.DecoratingProxy");
							logger.debug("creating native JDKProxy configuration for these interfaces: "+interfaces);
							registry.proxy().add(NativeProxyEntry.ofInterfaceNames(interfaces.toArray(new String[0])));
						}
					} else if (!beanType.isInterface()) {
						// events sample requires this kind of proxy:
						// @AotProxyHint(targetClass=com.example.events.SamplePublisher.class, 
						//  proxyFeatures = ProxyBits.IS_STATIC)
						logger.debug("creating AOTProxy for this class: "+beanType.getName());
						registry.proxy().add(NativeProxyEntry.ofClass(beanType,ProxyBits.IS_STATIC));
					}
				});
		}
	}
	
}
