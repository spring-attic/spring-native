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
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.util.Assert;

/**
 * {@code TestExecutionListener} which provides dependency injection support
 * for test instances whose {@code ApplicationContext} is loaded via AOT
 * mechanisms.
 *
 * @author Sam Brannen
 * @see DependencyInjectionTestExecutionListener
 * @see AotContextLoader
 * @see AotCacheAwareContextLoaderDelegate
 */
public class AotDependencyInjectionTestExecutionListener extends AbstractTestExecutionListener {

	private static final Log logger = LogFactory.getLog(AotDependencyInjectionTestExecutionListener.class);

	private static final AotContextLoader aotContextLoader = new AotContextLoader();


	/**
	 * Returns {@code 1999}, which is one less than the order configured for the
	 * standard {@link DependencyInjectionTestExecutionListener}, thereby
	 * ensuring that the {@code AotDependencyInjectionTestExecutionListener}
	 * gets a chance to perform dependency injection before the
	 * {@code DependencyInjectionTestExecutionListener} clears the
	 * {@link DependencyInjectionTestExecutionListener#REINJECT_DEPENDENCIES_ATTRIBUTE
	 * REINJECT_DEPENDENCIES_ATTRIBUTE} flag.
	 */
	@Override
	public final int getOrder() {
		return 1999;
	}

	@Override
	public void prepareTestInstance(TestContext testContext) throws Exception {
		if (aotContextLoader.isSupportedTestClass(testContext.getTestClass())) {
			if (logger.isDebugEnabled()) {
				logger.debug("Performing dependency injection for test context [" + testContext + "].");
			}
			injectDependencies(testContext);
		}
	}

	@Override
	public void beforeTestMethod(TestContext testContext) throws Exception {
		if (aotContextLoader.isSupportedTestClass(testContext.getTestClass())) {
			if (Boolean.TRUE.equals(
				testContext.getAttribute(DependencyInjectionTestExecutionListener.REINJECT_DEPENDENCIES_ATTRIBUTE))) {
				if (logger.isDebugEnabled()) {
					logger.debug("Reinjecting dependencies for test context [" + testContext + "].");
				}
				injectDependencies(testContext);
			}
		}
	}

	protected void injectDependencies(TestContext testContext) throws Exception {
		ApplicationContext applicationContext = testContext.getApplicationContext();
		Assert.state(applicationContext instanceof GenericApplicationContext,
				() -> "AOT ApplicationContext must be a GenericApplicationContext instead of " +
						applicationContext.getClass().getName());
		ConfigurableListableBeanFactory beanFactory = ((GenericApplicationContext) applicationContext).getBeanFactory();
		AutowiredAnnotationBeanPostProcessor beanPostProcessor = new AutowiredAnnotationBeanPostProcessor();
		beanPostProcessor.setBeanFactory(beanFactory);
		beanPostProcessor.processInjection(testContext.getTestInstance());
	}

}
