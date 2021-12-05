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
package org.springframework.context.annotation;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.scope.ScopedObject;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanFactoryNativeConfigurationProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeProxyEntry;
import org.springframework.aot.support.BeanFactoryProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.nativex.hint.AotProxyHint;
import org.springframework.nativex.hint.ProxyBits;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Detect and register {@link AotProxyHint} for beans annotated with {@link Scope} with mode set to
 * {@link ScopedProxyMode#TARGET_CLASS} or meta-annotated like {@link RequestScope}.
 *
 * TODO Support ScopedProxyMode.INTERFACES
 *
 * @author Sébastien Deleuze
 */
public class ScopeNativeConfigurationProcessor implements BeanFactoryNativeConfigurationProcessor {

	private static final Log logger = LogFactory.getLog(ScopeNativeConfigurationProcessor.class);

	@Override
	public void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
		new BeanFactoryProcessor(beanFactory).processBeansWithAnnotation(Scope.class, (beanName, beanType) -> {
			Scope scope = beanFactory.findAnnotationOnBean(beanName, Scope.class);
			if (scope.proxyMode() == ScopedProxyMode.TARGET_CLASS) {
				registry.proxy().add(NativeProxyEntry.ofClass(beanType, ProxyBits.NONE,
						ScopedObject.class, Serializable.class, AopInfrastructureBean.class));
			}
			else if (scope.proxyMode() == ScopedProxyMode.INTERFACES) {
				logger.warn("Inferring JDK dynamic proxy for " + beanName + " bean annotated with @Scope is not supported yet");
			}
		});
	}
}
