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

package org.springframework.test;

import org.springframework.nativex.hint.Flag;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

/**
 * Native hints for Spring Framework's testing support.
 *
 * @author Sam Brannen
 * @see org.springframework.boot.test.SpringBootTestHints
 */
@NativeHint(trigger = org.junit.jupiter.api.Test.class,
	types = {
		@TypeHint(types = {
			org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.class,
			org.springframework.test.context.junit.jupiter.SpringExtension.class,
			org.springframework.test.context.support.DefaultBootstrapContext.class,
			org.springframework.test.context.support.DefaultTestContextBootstrapper.class,
			org.springframework.test.context.web.WebTestContextBootstrapper.class
		}),
		@TypeHint(types = {
			org.springframework.test.context.ActiveProfiles.class,
			org.springframework.test.context.BootstrapWith.class,
			org.springframework.test.context.ContextConfiguration.class,
			org.springframework.test.context.ContextHierarchy.class,
			org.springframework.test.context.DynamicPropertySource.class,
			org.springframework.test.context.NestedTestConfiguration.class,
			org.springframework.test.context.TestConstructor.class,
			org.springframework.test.context.TestExecutionListeners.class,
			org.springframework.test.context.TestPropertySource.class,
			org.springframework.test.context.event.RecordApplicationEvents.class,
			org.springframework.test.context.junit.jupiter.EnabledIf.class,
			org.springframework.test.context.junit.jupiter.DisabledIf.class,
			org.springframework.test.context.junit.jupiter.SpringJUnitConfig.class,
			org.springframework.test.context.transaction.AfterTransaction.class,
			org.springframework.test.context.transaction.BeforeTransaction.class,
			org.springframework.test.context.web.WebAppConfiguration.class
		}, access = Flag.allPublicMethods),
		@TypeHint(types = {
				org.springframework.test.context.support.DelegatingSmartContextLoader.class
		}, access = Flag.allDeclaredConstructors)
	},
	jdkProxies = {
		@JdkProxyHint(types = { org.springframework.test.context.ActiveProfiles.class, org.springframework.core.annotation.SynthesizedAnnotation.class }),
		@JdkProxyHint(types = { org.springframework.test.context.BootstrapWith.class, org.springframework.core.annotation.SynthesizedAnnotation.class }),
		@JdkProxyHint(types = { org.springframework.test.context.ContextConfiguration.class, org.springframework.core.annotation.SynthesizedAnnotation.class }),
		@JdkProxyHint(types = { org.springframework.test.context.ContextHierarchy.class, org.springframework.core.annotation.SynthesizedAnnotation.class }),
		@JdkProxyHint(types = { org.springframework.test.context.NestedTestConfiguration.class, org.springframework.core.annotation.SynthesizedAnnotation.class }),
		@JdkProxyHint(types = { org.springframework.test.context.TestConstructor.class, org.springframework.core.annotation.SynthesizedAnnotation.class }),
		@JdkProxyHint(types = { org.springframework.test.context.TestExecutionListeners.class, org.springframework.core.annotation.SynthesizedAnnotation.class }),
		@JdkProxyHint(types = { org.springframework.test.context.TestPropertySource.class, org.springframework.core.annotation.SynthesizedAnnotation.class }),
		@JdkProxyHint(types = { org.springframework.test.context.event.RecordApplicationEvents.class, org.springframework.core.annotation.SynthesizedAnnotation.class }),
		@JdkProxyHint(types = { org.springframework.test.context.junit.jupiter.EnabledIf.class, org.springframework.core.annotation.SynthesizedAnnotation.class }),
		@JdkProxyHint(types = { org.springframework.test.context.junit.jupiter.DisabledIf.class, org.springframework.core.annotation.SynthesizedAnnotation.class }),
		@JdkProxyHint(types = { org.springframework.test.context.web.WebAppConfiguration.class, org.springframework.core.annotation.SynthesizedAnnotation.class }),
		// TODO Determine if we still need to add a JDK proxy hint for @ComponentScan.Filter when testing with AOT support.
		@JdkProxyHint(typeNames = { "org.springframework.context.annotation.ComponentScan$Filter", "org.springframework.core.annotation.SynthesizedAnnotation" })
	}
)

@NativeHint(trigger = org.springframework.test.context.jdbc.SqlScriptsTestExecutionListener.class,
	types = {
		@TypeHint(types = {
			org.springframework.test.context.jdbc.Sql.class,
			org.springframework.test.context.jdbc.SqlConfig.class,
			org.springframework.test.context.jdbc.SqlGroup.class,
			org.springframework.test.context.jdbc.SqlMergeMode.class
		}, access = Flag.allPublicMethods)
	},
	jdkProxies = {
		@JdkProxyHint(types = { org.springframework.test.context.jdbc.Sql.class, org.springframework.core.annotation.SynthesizedAnnotation.class }),
		@JdkProxyHint(types = { org.springframework.test.context.jdbc.SqlConfig.class, org.springframework.core.annotation.SynthesizedAnnotation.class }),
		@JdkProxyHint(types = { org.springframework.test.context.jdbc.SqlGroup.class, org.springframework.core.annotation.SynthesizedAnnotation.class }),
		@JdkProxyHint(types = { org.springframework.test.context.jdbc.SqlMergeMode.class, org.springframework.core.annotation.SynthesizedAnnotation.class })
	}
)
public class SpringTestHints implements NativeConfiguration {
}
