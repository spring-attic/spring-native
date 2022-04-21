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

package org.springframework.validation.annotation;

import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;

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
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.nativex.hint.ProxyBits;
import org.springframework.transaction.annotation.TransactionalNativeConfigurationProcessor;
import org.springframework.util.ClassUtils;

/**
 * Recognize components that need validation related proxy entries and register them.
 *
 * @author Petr Hejl
 * @author Andy Clement
 */
	public class ValidatedNativeConfigurationProcessor implements BeanFactoryNativeConfigurationProcessor {

	private static Log logger = LogFactory.getLog(ValidatedNativeConfigurationProcessor.class);

	protected final static String VALIDATED_CLASS_NAME = "org.springframework.validation.annotation.Validated";


	@Override
	public void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
		if (ClassUtils.isPresent(VALIDATED_CLASS_NAME, beanFactory.getBeanClassLoader())) {
			new Processor().process(beanFactory, registry);
		}
	}

	private static class Processor {

		void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
			new BeanFactoryProcessor(beanFactory).processBeans(this::isValidated,
				(beanName, beanType) -> {
					if (TransactionalNativeConfigurationProcessor.hasInterfaceMethods(beanType)) {
						LinkedHashSet<String> interfaces = new LinkedHashSet<>();
						TransactionalNativeConfigurationProcessor.collectInterfaces(beanType, interfaces);
						if (interfaces.size()!=0) {
							interfaces.add(SpringProxy.class.getName());
							interfaces.add(Advised.class.getName());
							interfaces.add(DecoratingProxy.class.getName());
							logger.debug("creating native JDKProxy configuration for these interfaces: "+interfaces);
							registry.proxy().add(NativeProxyEntry.ofInterfaceNames(interfaces.toArray(new String[0])));
						}
					} else if (!beanType.isInterface() && !Modifier.isFinal(beanType.getModifiers())) { // record types are final
						logger.debug("creating AOTProxy for this class: "+beanType.getName());
						registry.proxy().add(NativeProxyEntry.ofClass(beanType,ProxyBits.IS_STATIC));
					}
				});
		}

		public boolean isValidated(Class<?> beanType) {
			MergedAnnotations mergedAnnotations = MergedAnnotations.from(beanType,SearchStrategy.TYPE_HIERARCHY);
			return mergedAnnotations.get(VALIDATED_CLASS_NAME).isPresent();
		}
	}

}
