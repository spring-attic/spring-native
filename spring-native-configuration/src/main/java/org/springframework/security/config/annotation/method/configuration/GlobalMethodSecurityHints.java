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

package org.springframework.security.config.annotation.method.configuration;

import org.springframework.context.annotation.AutoProxyRegistrar;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.security.access.intercept.aopalliance.MethodSecurityMetadataSourceAdvisor;

@NativeHint(trigger=GlobalMethodSecuritySelector.class, types = {
		@TypeHint(types= {
				EnableGlobalMethodSecurity.class,
				GlobalMethodSecurityConfiguration.class,
				AutoProxyRegistrar.class,
				GlobalMethodSecurityAspectJAutoProxyRegistrar.class,
				MethodSecurityMetadataSourceAdvisorRegistrar.class,
				Jsr250MetadataSourceConfiguration.class,
		}),
		@TypeHint(types = MethodSecurityMetadataSourceAdvisor.class, access = AccessBits.CLASS | AccessBits.PUBLIC_METHODS | AccessBits.DECLARED_CONSTRUCTORS),
		@TypeHint(typeNames= "org.springframework.security.access.expression.method.MethodSecurityExpressionRoot", access = AccessBits.DECLARED_CONSTRUCTORS),
})
@NativeHint(trigger = ReactiveMethodSecuritySelector.class, types = @TypeHint(types= {
		AutoProxyRegistrar.class,
		ReactiveMethodSecurityConfiguration.class}))
@NativeHint(jdkProxies = @JdkProxyHint(types = {
		org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication.class,
		org.springframework.core.annotation.SynthesizedAnnotation.class
}))
public class GlobalMethodSecurityHints implements NativeConfiguration { }