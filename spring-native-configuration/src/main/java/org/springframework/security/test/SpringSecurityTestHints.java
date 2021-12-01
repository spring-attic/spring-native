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

package org.springframework.security.test;

import org.springframework.nativex.hint.FieldHint;
import org.springframework.nativex.hint.Flag;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

/**
 * Native hints for Spring Security's testing support.
 *
 * @see org.springframework.test.SpringTestHints
 * @see org.springframework.boot.test.SpringBootTestHints
 */
@NativeHint(trigger = org.springframework.security.test.context.support.WithSecurityContext.class,
	types = {
		@TypeHint(types = org.springframework.security.web.FilterChainProxy.class, access = { Flag.allDeclaredConstructors, Flag.allDeclaredMethods }),
		@TypeHint(types = org.springframework.security.web.context.SecurityContextPersistenceFilter.class, access = { Flag.allDeclaredConstructors, Flag.allDeclaredMethods, Flag.allPublicMethods }),
		@TypeHint(types = org.springframework.security.web.csrf.CsrfFilter.class, access = { Flag.allDeclaredConstructors, Flag.allDeclaredMethods, Flag.allPublicMethods }, fields = @FieldHint(name = "tokenRepository", allowWrite = true)),
		@TypeHint(types = org.springframework.security.test.context.support.WithSecurityContext.class, access = Flag.allPublicMethods),
		@TypeHint(typeNames = "org.springframework.security.test.context.support.WithMockUserSecurityContextFactory"),
		@TypeHint(typeNames = "reactor.core.publisher.Hooks")
	},
	jdkProxies = {
		@JdkProxyHint(types = { org.springframework.security.test.context.support.WithSecurityContext.class, org.springframework.core.annotation.SynthesizedAnnotation.class })
	}
)
public class SpringSecurityTestHints implements NativeConfiguration {
}
