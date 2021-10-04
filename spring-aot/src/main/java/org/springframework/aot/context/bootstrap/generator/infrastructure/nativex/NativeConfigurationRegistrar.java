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

package org.springframework.aot.context.bootstrap.generator.infrastructure.nativex;

import java.util.List;

import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * Register native configuration using processors detected on the classpath. Both
 * {@link BeanFactoryNativeConfigurationProcessor} and
 * {@link BeanNativeConfigurationProcessor} are handled.
 *
 * @author Stephane Nicoll
 */
public class NativeConfigurationRegistrar {

	private final ConfigurableListableBeanFactory beanFactory;

	private final List<BeanFactoryNativeConfigurationProcessor> beanFactoryProcessors;

	NativeConfigurationRegistrar(ConfigurableListableBeanFactory beanFactory,
			List<BeanFactoryNativeConfigurationProcessor> beanFactoryProcessors) {
		this.beanFactory = beanFactory;
		this.beanFactoryProcessors = beanFactoryProcessors;
	}

	public NativeConfigurationRegistrar(ConfigurableListableBeanFactory beanFactory) {
		this(beanFactory, SpringFactoriesLoader.loadFactories(
				BeanFactoryNativeConfigurationProcessor.class, beanFactory.getBeanClassLoader()));
	}

	/**
	 * Process the bean factory against the specified {@link NativeConfigurationRegistry}.
	 * @param registry the registry to use
	 */
	public void processBeanFactory(NativeConfigurationRegistry registry) {
		this.beanFactoryProcessors.forEach((processor) -> processor.process(this.beanFactory, registry));
	}

	/**
	 * Process the {@link BeanInstanceDescriptor bean instance descriptors} against the
	 * specified {@link NativeConfigurationRegistry}.
	 * @param registry the registry to use
	 * @param beans the bean instance descriptors
	 */
	public void processBeans(NativeConfigurationRegistry registry, Iterable<BeanInstanceDescriptor> beans) {
		List<BeanNativeConfigurationProcessor> beanProcessors = loadBeanProcessors(this.beanFactory);
		beanProcessors.forEach((processor) -> beans.forEach((bean) -> processor.process(bean, registry)));
	}

	protected List<BeanNativeConfigurationProcessor> loadBeanProcessors(ConfigurableListableBeanFactory beanFactory) {
		List<BeanNativeConfigurationProcessor> processors = SpringFactoriesLoader.loadFactories(
				BeanNativeConfigurationProcessor.class, beanFactory.getBeanClassLoader());
		for (BeanNativeConfigurationProcessor processor : processors) {
			if (processor instanceof BeanFactoryAware) {
				((BeanFactoryAware) processor).setBeanFactory(beanFactory);
			}
		}
		return processors;
	}

}
