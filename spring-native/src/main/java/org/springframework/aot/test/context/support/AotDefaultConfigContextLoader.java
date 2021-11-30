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

package org.springframework.aot.test.context.support;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.support.AbstractGenericContextLoader;

/**
 * An {@link AbstractGenericContextLoader} that use an
 * {@link ApplicationContextInitializer} to bootstrap a Spring test. This includes support
 * for the test context framework.

 * @author Stephane Nicoll
 */
public class AotDefaultConfigContextLoader extends AbstractGenericContextLoader {

	private final Class<? extends ApplicationContextInitializer<?>> testContextInitializer;

	public AotDefaultConfigContextLoader(Class<? extends ApplicationContextInitializer<?>> testContextInitializer) {
		this.testContextInitializer = testContextInitializer;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void loadBeanDefinitions(GenericApplicationContext context, MergedContextConfiguration mergedConfig) {
		ApplicationContextInitializer applicationContextInitializer = BeanUtils.instantiateClass(testContextInitializer);
		applicationContextInitializer.initialize(context);
	}

	@Override
	protected void customizeContext(GenericApplicationContext context) {
		// Undo hard-coded registration of annotation-based infra
		safeRemoveBeanDefinitions(context,
				AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME,
				AnnotationConfigUtils.COMMON_ANNOTATION_PROCESSOR_BEAN_NAME,
				AnnotationConfigUtils.CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME,
				AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR,
				AnnotationConfigUtils.EVENT_LISTENER_FACTORY_BEAN_NAME,
				AnnotationConfigUtils.EVENT_LISTENER_PROCESSOR_BEAN_NAME);
	}

	private void safeRemoveBeanDefinitions(GenericApplicationContext context, String... names) {
		for (String name : names) {
			if (context.containsBeanDefinition(name)) {
				context.removeBeanDefinition(name);
			}
		}
	}

	@Override
	protected BeanDefinitionReader createBeanDefinitionReader(GenericApplicationContext context) {
		throw new UnsupportedOperationException(
				"AotAnnotationConfigContextLoader does not support the createBeanDefinitionReader(GenericApplicationContext) method");
	}

	@Override
	protected String getResourceSuffix() {
		throw new UnsupportedOperationException(
				"AotAnnotationConfigContextLoader does not support the getResourceSuffix() method");
	}
}
