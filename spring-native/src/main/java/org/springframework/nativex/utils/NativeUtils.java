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

package org.springframework.nativex.utils;

import java.util.Properties;

public abstract class NativeUtils {

	public static Properties getNativeProperties() {
		Properties props = new Properties();
		props.put("spring.aop.proxy-target-class", "false"); // Not supported in native images
		props.put("spring.cloud.refresh.enabled", "false"); // Sampler is a class and can't be proxied
		props.put("spring.sleuth.async.enabled", "false"); // Too much proxy created
		props.put("spring.cloud.compatibility-verifier.enabled", "false"); // To avoid false positive due to SpringApplication patched copy
		props.put("spring.devtools.restart.enabled", "false"); // Deactivate dev tools
		return props;
	}
}
