/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.boot;

import java.util.logging.LogManager;

import org.springframework.boot.logging.java.JavaLoggingSystem;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.ResourcesInfo;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;

@NativeImageHint(
	resourcesInfos = {
		@ResourcesInfo(patterns= {
			"db/.*", // TODO should be conditional on database active?
			"messages/.*",
			"banner.txt",
			"META-INF/spring.components",
			"application.yml",
			"application.properties",
			// This one originally added for kotlin but covers many other scenarios too - is it too many files?
			"META-INF/services/.*",
		    // Do these two catch the logging/<eachDirectory>/*.properties?
		    "org/springframework/boot/logging/.*.properties",
		    "org/springframework/boot/logging/.*.xml",
		    "logging.properties",
		    "org/springframework/boot/logging/java/logging.properties"
			})
	},
	typeInfos = { 
		@TypeInfo(types = {
			SpringApplication.class,
			SpringBootConfiguration.class,
			LogManager.class,
			JavaLoggingSystem.class
		}, access = AccessBits.LOAD_AND_CONSTRUCT)
	}
)
public class SpringApplicationHints implements NativeImageConfiguration {
}
