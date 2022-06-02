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

package org.springframework.nativex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.NativeDetector;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.nativex.utils.NativeUtils;
import org.springframework.util.ClassUtils;

/**
 * TODO Manage to avoid duplication with ContextBootstrapContributor
 *
 * @author Sebastien Deleuze
 */
public class NativeListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

	private static final Log logger = LogFactory.getLog(NativeListener.class);

	static {
		if (NativeDetector.inNativeImage() && ClassUtils.isPresent("org.hibernate.Session", null)) {
			System.setProperty("hibernate.bytecode.provider", "none");
		}
	}

	public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
		if (AotModeDetector.isAotModeEnabled() || AotModeDetector.isRunningAotTests()) {
			logger.info("AOT mode enabled");
			ConfigurableEnvironment environment = event.getEnvironment();
			environment.getPropertySources().addFirst(new PropertiesPropertySource("native", NativeUtils.getNativeProperties()));
		}
		else {
			logger.info("AOT mode disabled");
		}
	}
}
