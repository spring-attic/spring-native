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

package org.springframework.aot.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.nativex.AotModeDetector;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.util.Assert;

/**
 * {@code TestExecutionListener} which provides dependency injection for AOT
 * generated application contexts for supported test classes.
 *
 * <p>For all other test classes, this listener delegates to the standard support
 * in {@link DependencyInjectionTestExecutionListener}. This listener can therefore
 * be used as a drop-in replacement for {@code DependencyInjectionTestExecutionListener}.
 *
 * @author Sam Brannen
 * @see DependencyInjectionTestExecutionListener
 * @see AotContextLoader
 * @see AotCacheAwareContextLoaderDelegate
 */
public class AotDependencyInjectionTestExecutionListener extends DependencyInjectionTestExecutionListener {

	private static final Log logger = LogFactory.getLog(AotDependencyInjectionTestExecutionListener.class);

	private static final AotContextLoader aotContextLoader = getAotContextLoader();


	@Override
	public void prepareTestInstance(TestContext testContext) throws Exception {
		if (isSupportedTestClass(testContext)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Performing dependency injection for test context [" + testContext + "].");
			}
			injectDependenciesInAotMode(testContext);
		}
		else {
			super.prepareTestInstance(testContext);
		}
	}

	@Override
	public void beforeTestMethod(TestContext testContext) throws Exception {
		if (isSupportedTestClass(testContext)) {
			if (Boolean.TRUE.equals(testContext.getAttribute(REINJECT_DEPENDENCIES_ATTRIBUTE))) {
				if (logger.isDebugEnabled()) {
					logger.debug("Reinjecting dependencies for test context [" + testContext + "].");
				}
				injectDependenciesInAotMode(testContext);
			}
		}
		else {
			super.beforeTestMethod(testContext);
		}
	}


	private void injectDependenciesInAotMode(TestContext testContext) throws Exception {
		Class<?> clazz = testContext.getTestClass();
		Object bean = testContext.getTestInstance();
		ApplicationContext applicationContext = testContext.getApplicationContext();
		Assert.state(applicationContext instanceof GenericApplicationContext,
				() -> "AOT ApplicationContext must be a GenericApplicationContext instead of " +
						applicationContext.getClass().getName());

		ConfigurableListableBeanFactory beanFactory = ((GenericApplicationContext) applicationContext).getBeanFactory();
		AutowiredAnnotationBeanPostProcessor beanPostProcessor = new AutowiredAnnotationBeanPostProcessor();
		beanPostProcessor.setBeanFactory(beanFactory);
		beanPostProcessor.processInjection(bean);
		beanFactory.initializeBean(bean, clazz.getName() + AutowireCapableBeanFactory.ORIGINAL_INSTANCE_SUFFIX);

		testContext.removeAttribute(REINJECT_DEPENDENCIES_ATTRIBUTE);
	}

	private boolean isSupportedTestClass(TestContext testContext) {
		return aotContextLoader != null && aotContextLoader.isSupportedTestClass(testContext.getTestClass());
	}

	private static AotContextLoader getAotContextLoader() {
		if (AotModeDetector.isRunningAotTests()) {
			try {
				return new AotContextLoader();
			}
			catch (Exception ex) {
				throw new IllegalStateException("Failed to instantiate AotContextLoader", ex);
			}
		}
		return null;
	}

}
