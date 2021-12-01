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

package org.springframework.boot.test;

import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.nativex.hint.Flag;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.test.web.client.SimpleRequestExpectationManager;

/**
 * Native hints for Spring Boot's testing support.
 *
 * @see org.springframework.test.SpringTestHints
 */
@NativeHint(trigger = org.junit.jupiter.api.Test.class,
	types = {
		@TypeHint(types = {
			org.springframework.aot.test.AotCacheAwareContextLoaderDelegate.class,
			org.springframework.boot.autoconfigure.ImportAutoConfiguration.class,
			org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration.class,
			org.springframework.boot.test.autoconfigure.SpringBootDependencyInjectionTestExecutionListener.class,
			org.springframework.boot.test.autoconfigure.filter.TypeExcludeFilters.class,
			org.springframework.boot.test.context.SpringBootContextLoader.class,
			org.springframework.boot.test.context.SpringBootTest.WebEnvironment.class,
			org.springframework.boot.test.mock.mockito.MockitoPostProcessor.class
		}, typeNames = {
			"org.springframework.boot.autoconfigure.test.ImportAutoConfiguration",
			"org.springframework.boot.test.mock.mockito.MockitoPostProcessor$SpyPostProcessor",
			"org.springframework.boot.test.context.ImportsContextCustomizer$ImportsCleanupPostProcessor"
		}),
		@TypeHint(types = {
			org.springframework.boot.SpringBootConfiguration.class,
			org.springframework.boot.test.context.SpringBootTest.class,
			org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc.class,
		}, access = Flag.allPublicMethods)
	},
	jdkProxies = {
		@JdkProxyHint(types = { org.springframework.context.annotation.Import.class, org.springframework.core.annotation.SynthesizedAnnotation.class }),
		@JdkProxyHint(types = { org.springframework.boot.test.context.SpringBootTest.class, org.springframework.core.annotation.SynthesizedAnnotation.class }),
		@JdkProxyHint(types = { org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration.class, org.springframework.core.annotation.SynthesizedAnnotation.class }),
		@JdkProxyHint(types = { org.springframework.boot.test.autoconfigure.filter.TypeExcludeFilters.class, org.springframework.core.annotation.SynthesizedAnnotation.class }),
	}
)
@NativeHint(trigger = org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration.class,
	types = @TypeHint(typeNames = "org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration$EmbeddedDataSourceFactoryBean"))
@NativeHint(trigger = MockServerRestTemplateCustomizer.class, types = @TypeHint(types = SimpleRequestExpectationManager.class))
public class SpringBootTestHints implements NativeConfiguration {
}
