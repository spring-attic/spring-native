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

import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.MethodHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.security.config.annotation.SecurityBuilder;
import org.springframework.security.config.annotation.SecurityConfigurer;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configuration.OAuth2ClientConfiguration.OAuth2ClientWebMvcImportSelector;
import org.springframework.security.config.annotation.web.configuration.OAuth2ClientConfiguration.OAuth2ClientWebMvcSecurityConfiguration;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer.JwtConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.web.servlet.DispatcherServlet;

@NativeHint(trigger = OAuth2AuthorizationServerConfiguration.class, types = 
	@TypeHint(types = OAuth2AuthorizationCodeAuthenticationProvider.class,
		methods = @MethodHint(name="setProviderSettings",parameterTypes=ProviderSettings.class)))
@NativeHint(trigger = SpringWebMvcImportSelector.class, follow = true, types = {
		@TypeHint(types= DispatcherServlet.class, access = AccessBits.CLASS),
		@TypeHint(types = WebMvcSecurityConfiguration.class)
})
@NativeHint(trigger = OAuth2ImportSelector.class, follow = true, types = {
		@TypeHint(types= ClientRegistration.class,access=AccessBits.CLASS),
		@TypeHint(types= OAuth2ClientConfiguration.class),
		@TypeHint(types = {
				JwtConfigurer.class,
				OAuth2ResourceServerConfigurer.class,
				AbstractHttpConfigurer.class,
				HttpSecurityBuilder.class,
				SecurityConfigurerAdapter.class,
				SecurityConfigurer.class,
				SecurityBuilder.class,
				DefaultSecurityFilterChain.class})
})
// from gs-securing-web sample
@NativeHint(trigger = OAuth2ClientWebMvcImportSelector.class, types = {
	@TypeHint(types= {
			OAuth2ClientWebMvcSecurityConfiguration.class,
			DispatcherServlet.class})
})
public class WebMvcSecurityHints implements NativeConfiguration {
}
