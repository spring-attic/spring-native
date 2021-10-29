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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanFactoryNativeConfigurationProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeProxyEntry;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.AotProxyNativeConfigurationProcessor;
import org.springframework.core.DecoratingProxy;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.nativex.hint.ProxyBits;
import org.springframework.util.ClassUtils;

/**
 * Recognize components that need transactional proxies and register them.
 *
 * @author Andy Clement
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

	public static boolean hasAnnotatedMethods(Class<?> type, String annotationName) {
		for (Method method: type.getDeclaredMethods()) {
			MergedAnnotations methodAnnotations = MergedAnnotations.from(method);
			boolean hasAnnotation = methodAnnotations.get(annotationName).isPresent();
			if (hasAnnotation) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isTransactional(Class<?> type) {
		MergedAnnotations typeAnnotations = MergedAnnotations.from(type);
		return (typeAnnotations.get(TRANSACTIONAL_CLASS_NAME).isPresent() || typeAnnotations.get(JAVAX_TRANSACTIONAL_CLASS_NAME).isPresent());
	}
	
	private static class Processor {

		// TODO rationalize why some scenarios (jdbc-tx) need the JDKProxy whilst some (events) need the AotProxy and add
		// intelligence here that captures the reason
		void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
			AotProxyNativeConfigurationProcessor.doWithComponents(beanFactory,
				(beanName, beanType) -> {
						List<String> transactionalInterfaces = new ArrayList<>();
						for (Class<?> intface: beanType.getInterfaces()) {
							transactionalInterfaces.add(intface.getName());
						}
						// jdbc-tx sample requires this kind of JDK Proxy
						//  [interface org.springframework.boot.CommandLineRunner, 
						//   interface app.main.Finder, 
						//   interface org.springframework.aop.SpringProxy, 
						//   interface org.springframework.aop.framework.Advised, 
						//   interface org.springframework.core.DecoratingProxy]
						if (transactionalInterfaces.size()!=0) {
							transactionalInterfaces.add(SpringProxy.class.getName());
							transactionalInterfaces.add(Advised.class.getName());//"org.springframework.aop.framework.Advised");
							transactionalInterfaces.add(DecoratingProxy.class.getName());//"org.springframework.core.DecoratingProxy");
							logger.debug("creating native JDKProxy configuration for these interfaces: "+transactionalInterfaces);
							registry.proxy().add(NativeProxyEntry.ofInterfaceNames(transactionalInterfaces.toArray(new String[0])));
						}
						// events sample requires this kind of proxy:
						// @AotProxyHint(targetClass=com.example.events.SamplePublisher.class, 
						//  proxyFeatures = ProxyBits.IS_STATIC)
						if (!beanType.isInterface()) {
							logger.debug("creating AOTProxy for this class: "+beanType.getName());
							registry.proxy().add(NativeProxyEntry.ofClass(beanType,ProxyBits.IS_STATIC));
						}
				},
				(beanName, beanType) -> {
					return isTransactional(beanType) || hasAnnotatedMethods(beanType, TRANSACTIONAL_CLASS_NAME) || hasAnnotatedMethods(beanType, JAVAX_TRANSACTIONAL_CLASS_NAME);
				});
		}
	}
	
}
