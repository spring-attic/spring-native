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

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.NativeDetector;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.util.ClassUtils;

/**
 * @author Sebastien Deleuze
 */
public class NativeListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

	private static final Log logger = LogFactory.getLog(NativeListener.class);

	static {
		if (!NativeDetector.inNativeImage()) {
			System.setProperty("org.graalvm.nativeimage.imagecode", "runtime");
		}
		if (ClassUtils.isPresent("org.hibernate.Session", null)) {
			System.setProperty("hibernate.bytecode.provider", "none");
		}
	}

	public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
		if (AotModeDetector.isAotModeEnabled()) {
			logger.info("This application is bootstrapped with code generated with Spring AOT");
			ConfigurableEnvironment environment = event.getEnvironment();
			Properties props = new Properties();
			props.put("spring.aop.proxy-target-class", "false"); // Not supported in native images
			props.put("spring.cloud.refresh.enabled", "false"); // Sampler is a class and can't be proxied
			props.put("spring.sleuth.async.enabled", "false"); // Too much proxy created
			props.put("spring.devtools.restart.enabled", "false"); // Deactivate dev tools
			environment.getPropertySources().addFirst(new PropertiesPropertySource("native", props));
		}
	}
}