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
import org.springframework.nativex.hint.InitializationInfo;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.hint.MethodInfo;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ProxyInfo;
import org.springframework.nativex.hint.ResourcesInfo;
import org.springframework.nativex.hint.TypeInfo;
import org.springframework.nativex.hint.AccessBits;


@NativeHint(
	resourcesInfos = {
		@ResourcesInfo(patterns= {
			"db/.*", // TODO should be conditional on database active?
			"messages/.*",
			"banner.txt",
			"META-INF/spring.components",
			"application.*.yml",
			"application.*.yaml",
			"^git.properties",
			"^META-INF/build-info.properties",
		    "^META-INF/spring-configuration-metadata.json",
		    "^META-INF/additional-spring-configuration-metadata.json",
			"application.*.properties",
			// This one originally added for kotlin but covers many other scenarios too - is it too many files?
			"META-INF/services/.*",
		    // Do these two catch the logging/<eachDirectory>/*.properties?
		    "org/springframework/boot/logging/.*.properties",
		    "org/springframework/boot/logging/.*.xml",
		    "logging.properties",
		    "org/springframework/boot/logging/java/logging.properties"
			}),
		@ResourcesInfo(patterns = {
			"messages/messages"	
			},isBundle = true)
	},
	typeInfos = { 
		@TypeInfo(types = {
			SpringBootConfiguration.class,
			LogManager.class,
			JavaLoggingSystem.class
		}, access = AccessBits.LOAD_AND_CONSTRUCT|AccessBits.PUBLIC_METHODS)
	},
	initializationInfos = @InitializationInfo(types = {
			org.springframework.boot.BeanDefinitionLoader.class,
			org.springframework.boot.logging.LoggingSystem.class,
			org.springframework.boot.liquibase.LiquibaseServiceLocatorApplicationListener.class,
			org.springframework.boot.logging.logback.LogbackLoggingSystem.class
	},
	typeNames = {
			"org.springframework.boot.autoconfigure.cache.CacheConfigurations",
			"org.springframework.boot.logging.java.JavaLoggingSystem$Factory",
			"org.springframework.boot.logging.log4j2.Log4J2LoggingSystem$Factory",
			"org.springframework.boot.logging.logback.LogbackLoggingSystem$Factory"
	}, initTime = InitializationTime.BUILD),
		proxyInfos = {
			@ProxyInfo(types = {
					org.springframework.boot.context.properties.ConfigurationProperties.class,
					org.springframework.core.annotation.SynthesizedAnnotation.class
			})
		}
)
@NativeHint(
		typeInfos = {
				@TypeInfo(types= SpringApplication.class, methods = {
						@MethodInfo(name="setBannerMode", parameterTypes = Banner.Mode.class) // Enables property control of banner mode
				},access=AccessBits.CLASS)
		})
public class SpringBootHints implements NativeConfiguration {
}
