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

package org.springframework.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanFactoryNativeConfigurationProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeProxyEntry;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.AotProxyNativeConfigurationProcessor;
import org.springframework.core.DecoratingProxy;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.nativex.hint.ProxyBits;
import org.springframework.util.ClassUtils;

/**
 * Recognize components that use any of PostAuthorize, PostFilter, PreAuthorize or PreFilter
 * annotations and register proxies for them.
 *
 * @author Eleftheria Stein
 */
public class PrePostSecuredNativeConfigurationProcessor implements BeanFactoryNativeConfigurationProcessor {

	private static Log logger = LogFactory.getLog(PrePostSecuredNativeConfigurationProcessor.class);

	public final static String PRE_AUTHORIZE = "org.springframework.security.access.prepost.PreAuthorize";
	public final static String PRE_FILTER = "org.springframework.security.access.prepost.PreFilter";
	public final static String POST_AUTHORIZE = "org.springframework.security.access.prepost.PostAuthorize";
	public final static String POST_FILTER = "org.springframework.security.access.prepost.PostFilter";

	@Override
	public void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
		if ((ClassUtils.isPresent(PRE_AUTHORIZE, beanFactory.getBeanClassLoader()) ||
				ClassUtils.isPresent(PRE_FILTER, beanFactory.getBeanClassLoader())) ||
				ClassUtils.isPresent(POST_AUTHORIZE, beanFactory.getBeanClassLoader()) ||
				ClassUtils.isPresent(POST_FILTER, beanFactory.getBeanClassLoader())) {
			new Processor().process(beanFactory, registry);
		}
	}

	private static boolean hasPrePostSecuredMethods(Class<?> type, SearchStrategy strategy) {
		for (Method method : type.getDeclaredMethods()) {
			MergedAnnotations methodAnnotations = MergedAnnotations.from(method, strategy);
			if (methodAnnotations.get(PRE_AUTHORIZE).isPresent() ||
					methodAnnotations.get(PRE_FILTER).isPresent() ||
					methodAnnotations.get(POST_AUTHORIZE).isPresent() ||
					methodAnnotations.get(POST_FILTER).isPresent()) {
				return true;
			}
		}
		return false;
	}

	private static boolean isPrePostSecured(Class<?> type, SearchStrategy strategy) {
		MergedAnnotations typeAnnotations = MergedAnnotations.from(type, strategy);
		return (typeAnnotations.get(PRE_AUTHORIZE).isPresent() ||
				typeAnnotations.get(PRE_FILTER).isPresent() ||
				typeAnnotations.get(POST_AUTHORIZE).isPresent() ||
				typeAnnotations.get(POST_FILTER).isPresent() ||
				hasPrePostSecuredMethods(type, strategy));
	}

	static class Processor {

		void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
			AotProxyNativeConfigurationProcessor.doWithComponents(beanFactory,
					(beanName, beanType) -> {
						// Check if it is an interface or the PrePost annotations are not directly on this 'class' (in which
						// case we assume it is on a super interface - this is not perfect)
						if (beanType.isInterface() || !isPrePostSecured(beanType, SearchStrategy.DIRECT)) {
							List<String> prePostSecuredInterfaces = new ArrayList<>();
							for (Class<?> intface : beanType.getInterfaces()) {
								prePostSecuredInterfaces.add(intface.getName());
							}
							if (prePostSecuredInterfaces.size() != 0) {
								prePostSecuredInterfaces.add(SpringProxy.class.getName());
								prePostSecuredInterfaces.add(Advised.class.getName());
								prePostSecuredInterfaces.add(DecoratingProxy.class.getName());
								logger.debug("creating native JDKProxy configuration for these interfaces: " + prePostSecuredInterfaces);
								registry.proxy().add(NativeProxyEntry.ofInterfaceNames(prePostSecuredInterfaces.toArray(new String[0])));
							}
						} else {
							logger.debug("creating AOTProxy for this class: " + beanType.getName());
							registry.proxy().add(NativeProxyEntry.ofClass(beanType, ProxyBits.IS_STATIC));
						}
					},
					(beanName, beanType) -> isPrePostSecured(beanType, SearchStrategy.TYPE_HIERARCHY));
		}
	}
}
