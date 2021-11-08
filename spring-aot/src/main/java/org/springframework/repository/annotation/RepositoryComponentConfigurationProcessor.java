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

package org.springframework.repository.annotation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanFactoryNativeConfigurationProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeProxyEntry;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.AotProxyNativeConfigurationProcessor.ComponentCallback;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.data.TypeUtils;
import org.springframework.nativex.hint.ProxyBits;
import org.springframework.stereotype.Repository;

/**
 * Recognize {@link Repository} annotated beans and creates a proxy for them.
 *
 * @author Christoph Strobl
 */
public class RepositoryComponentConfigurationProcessor implements BeanFactoryNativeConfigurationProcessor {

	private static Log logger = LogFactory.getLog(RepositoryComponentConfigurationProcessor.class);

	@Override
	public void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
		new Processor().process(beanFactory, registry);
	}

	private static class Processor {

		void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
			doWithComponents(beanFactory,
					(beanName, beanType) -> {
						logger.debug("Creating AOTProxy for @Repository component: " + beanType.getName());
						registry.proxy().add(NativeProxyEntry.ofClass(beanType, ProxyBits.IS_STATIC));
					});
		}

		static void doWithComponents(ConfigurableListableBeanFactory beanFactory, ComponentCallback callback) {
			beanFactory.getBeanNamesIterator().forEachRemaining((beanName) -> {
				Class<?> beanType = beanFactory.getType(beanName);
				if (MergedAnnotations.from(beanType).get(Repository.class).isPresent()) {
					callback.invoke(beanName, beanType);
				}
			});
		}
	}
}
