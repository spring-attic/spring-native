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
import org.springframework.test.context.CacheAwareContextLoaderDelegate;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.SmartContextLoader;
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate;

/**
 * A {@link CacheAwareContextLoaderDelegate} that enables the use of generated application
 * contexts for supported test classes.
 *
 * @author Stephane Nicoll
 * @author Sam Brannen
 */
public class AotCacheAwareContextLoaderDelegate extends DefaultCacheAwareContextLoaderDelegate {

	private static final Log logger = LogFactory.getLog(AotCacheAwareContextLoaderDelegate.class);

	@Override
	protected ApplicationContext loadContextInternal(MergedContextConfiguration config) throws Exception {
		SmartContextLoader contextLoader = AotContextLoaderUtils.getContextLoader(config.getTestClass());
		if (contextLoader != null) {
			logger.info("Starting test in AOT mode using " + contextLoader);
			return contextLoader.loadContext(config);
		}
		return super.loadContextInternal(config);
	}

}
