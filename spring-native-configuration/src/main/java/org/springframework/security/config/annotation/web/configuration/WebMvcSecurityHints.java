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
package org.springframework.security.config.annotation.web.configuration;

import org.springframework.nativex.hint.MethodHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.security.web.access.expression.WebSecurityExpressionRoot;

@NativeHint(trigger = OAuth2AuthorizationServerConfiguration.class, types =
	@TypeHint(types = OAuth2AuthorizationCodeAuthenticationProvider.class,
		methods = @MethodHint(name="setProviderSettings",parameterTypes=ProviderSettings.class)))
@NativeHint(trigger = WebSecurityConfiguration.class,
		// TODO interesting that gs-securing-web causes these to be needed although it is in thymeleaf (due to SpEL expressions I think)
		types = {@TypeHint(
				typeNames = "org.thymeleaf.standard.expression.RestrictedRequestAccessUtils$RestrictedRequestWrapper",
				access = {TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.PUBLIC_METHODS, TypeAccess.DECLARED_FIELDS}),
				@TypeHint(
						types = {WebSecurityExpressionRoot.class},
						access = {TypeAccess.DECLARED_METHODS, TypeAccess.DECLARED_FIELDS}
				)
		})
public class WebMvcSecurityHints implements NativeConfiguration {
}
