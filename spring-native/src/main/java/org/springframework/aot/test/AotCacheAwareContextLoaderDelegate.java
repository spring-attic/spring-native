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

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.test.context.CacheAwareContextLoaderDelegate;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.SmartContextLoader;
import org.springframework.test.context.cache.ContextCache;
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate;
import org.springframework.util.Assert;

/**
 * A {@link CacheAwareContextLoaderDelegate} that enables the use of AOT-generated
 * application contexts for supported test classes.
 *
 * @author Stephane Nicoll
 * @author Sam Brannen
 */
public class AotCacheAwareContextLoaderDelegate extends DefaultCacheAwareContextLoaderDelegate {

	private static final Log logger = LogFactory.getLog(AotCacheAwareContextLoaderDelegate.class);

	private final AotTestMappings aotTestMappings;

	public AotCacheAwareContextLoaderDelegate() {
		this.aotTestMappings = new AotTestMappings();
	}

	AotCacheAwareContextLoaderDelegate(AotTestMappings aotTestMappings, ContextCache contextCache) {
		super(contextCache);
		this.aotTestMappings = aotTestMappings;
	}

	@Override
	protected ApplicationContext loadContextInternal(MergedContextConfiguration config) throws Exception {
		SmartContextLoader contextLoader = this.aotTestMappings.getContextLoader(config.getTestClass());
		if (contextLoader != null) {
			Assert.isInstanceOf(AotMergedContextConfiguration.class, config);
			logger.info("Loading test ApplicationContext in AOT mode using " + contextLoader);
			return contextLoader.loadContext(((AotMergedContextConfiguration) config).getOriginal());
		}
		return super.loadContextInternal(config);
	}

	@Override
	public boolean isContextLoaded(MergedContextConfiguration mergedContextConfiguration) {
		return super.isContextLoaded(replaceIfNecessary(mergedContextConfiguration));
	}

	@Override
	public ApplicationContext loadContext(MergedContextConfiguration mergedContextConfiguration) {
		return super.loadContext(replaceIfNecessary(mergedContextConfiguration));
	}

	@Override
	public void closeContext(MergedContextConfiguration mergedContextConfiguration, HierarchyMode hierarchyMode) {
		super.closeContext(replaceIfNecessary(mergedContextConfiguration), hierarchyMode);
	}

	/**
	 * If the test class associated with the supplied {@link MergedContextConfiguration}
	 * has an AOT-generated {@link ApplicationContext}, this method will create an
	 * {@link AotMergedContextConfiguration} to replace the provided {@code MergedContextConfiguration}.
	 * <p>This allows for transparent {@link org.springframework.test.context.cache.ContextCache ContextCache}
	 * support for AOT-generated application contexts, including support for context
	 * hierarchies.
	 * <p>Otherwise, this method simply returns the supplied {@code MergedContextConfiguration}
	 * unmodified.
	 */
	private MergedContextConfiguration replaceIfNecessary(MergedContextConfiguration mergedContextConfiguration) {
		if (mergedContextConfiguration == null) {
			return null;
		}
		Class<?> testClass = mergedContextConfiguration.getTestClass();
		Class<? extends ApplicationContextInitializer<?>> contextInitializerClass =
				this.aotTestMappings.getContextInitializerClass(testClass);
		if (contextInitializerClass != null) {
			return new AotMergedContextConfiguration(testClass, contextInitializerClass, mergedContextConfiguration,
					this, replaceIfNecessary(mergedContextConfiguration.getParent()));
		}
		return mergedContextConfiguration;
	}

}
