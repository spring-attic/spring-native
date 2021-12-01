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

package org.springframework.boot.autoconfigure.security.servlet;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.nativex.AotOptions;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.security.config.annotation.web.configuration.AutowiredWebSecurityConfigurersIgnoreParents;
import org.springframework.util.ClassUtils;

@NativeHint(trigger=SecurityAutoConfiguration.class, types = {
		@TypeHint(typeNames = {
				"org.springframework.boot.autoconfigure.security.DefaultWebSecurityCondition",
				"org.springframework.boot.autoconfigure.security.DefaultWebSecurityCondition$Classes",
				"org.springframework.boot.autoconfigure.security.DefaultWebSecurityCondition$Beans",
		}, access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS, TypeAccess.PUBLIC_METHODS, TypeAccess.RESOURCE})
})
public class SecurityHints implements NativeConfiguration {
	@Override
	public void computeHints(NativeConfigurationRegistry registry, AotOptions aotOptions) {
		boolean javaxServletFilterAround = ClassUtils.isPresent("javax.servlet.Filter",null);
		boolean autowiredWebSecurityConfigurersIgnoreParentsAround = ClassUtils.isPresent("org.springframework.security.config.annotation.web.configuration.AutowiredWebSecurityConfigurersIgnoreParents",null);
		if (javaxServletFilterAround && autowiredWebSecurityConfigurersIgnoreParentsAround) {
			// This class includes methods that are called via SpEL and in a return value,  nested in generics, is a reference to javax.servlet.Filter
			registry.reflection().forType(AutowiredWebSecurityConfigurersIgnoreParents.class).withAccess(TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.PUBLIC_METHODS);
		}
	}
}
