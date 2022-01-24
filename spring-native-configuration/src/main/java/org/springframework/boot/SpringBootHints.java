/*
 * Copyright 2019-2022 the original author or authors.
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

package org.springframework.boot;

import java.util.logging.LogManager;

import org.springframework.boot.logging.java.JavaLoggingSystem;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.MethodHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;


@NativeHint(trigger = SpringApplication.class,
	resources = {
		@ResourceHint(patterns = {
			"META-INF/MANIFEST.MF",
			"db/.*",
			"messages/.*",
			"banner.txt",
			"META-INF/spring.components",
			"application.*.yml",
			"application.*.yaml",
			"^git.properties",
			"^META-INF/build-info.properties",
			"application.*.properties",
			"META-INF/services/.*",
			"org/springframework/boot/logging/.*.properties",
			"org/springframework/boot/logging/.*.xml",
			"logging.properties",
			"org/springframework/boot/logging/java/logging.properties"
			})
	},
	types = {
		@TypeHint(types = {
			LogManager.class,
			JavaLoggingSystem.class
		}, access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.PUBLIC_METHODS}),
			@TypeHint(types = {
					java.util.LinkedHashSet.class,
					java.util.ArrayList.class
			}), // Required for configuration properties binding since we do not process implementations
			@TypeHint(types= SpringApplication.class, methods = {
				@MethodHint(name="setBannerMode", parameterTypes = Banner.Mode.class), // Enables property control of banner mode
				@MethodHint(name="setWebApplicationType",parameterTypes = WebApplicationType.class)
			}, access = {})
	},
	initialization = @InitializationHint(types = {
			org.springframework.boot.BeanDefinitionLoader.class,
			org.springframework.boot.logging.LoggingSystem.class,
			org.springframework.boot.logging.logback.LogbackLoggingSystem.class
	},
	typeNames = {
			"org.springframework.boot.logging.java.JavaLoggingSystem$Factory",
			"org.springframework.boot.logging.log4j2.Log4J2LoggingSystem$Factory",
			"org.springframework.boot.logging.logback.LogbackLoggingSystem$Factory",
			"org.springframework.nativex.substitutions.boot.NativeSpringBootVersion"
	}, initTime = InitializationTime.BUILD),
		jdkProxies = {
			@JdkProxyHint(types = {
					org.springframework.boot.context.properties.ConfigurationProperties.class,
					org.springframework.core.annotation.SynthesizedAnnotation.class
			})
		}
)
public class SpringBootHints implements NativeConfiguration {
}
