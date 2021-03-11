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

package org.springframework.security.thymeleaf;

import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthorizationCodeAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.thymeleaf.extras.springsecurity5.dialect.processor.AuthorizeAttrProcessor;

@NativeHint(trigger = AuthorizeAttrProcessor.class,
		types = {
		@TypeHint(typeNames = "org.thymeleaf.extras.springsecurity5.util.Spring5VersionSpecificUtility", access = AccessBits.DECLARED_CONSTRUCTORS),
		@TypeHint(types = {
				UsernamePasswordAuthenticationToken.class,
				AbstractAuthenticationToken.class,
				OAuth2AuthenticationToken.class,
				OAuth2LoginAuthenticationToken.class,
				OAuth2AuthorizationCodeAuthenticationToken.class,
				UserDetails.class,
				AuthenticatedPrincipal.class,
				DefaultOAuth2User.class,
				DefaultOidcUser.class
		}, typeNames = {
				"org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken",
				"org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken"
		}, access = AccessBits.FULL_REFLECTION)}
)
public class ThymeleafSpringSecurity5Hints implements NativeConfiguration {
}