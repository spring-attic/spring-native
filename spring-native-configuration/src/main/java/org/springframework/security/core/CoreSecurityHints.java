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
package org.springframework.security.core;

import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.security.access.expression.SecurityExpressionOperations;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationFailureCredentialsExpiredEvent;
import org.springframework.security.authentication.event.AuthenticationFailureDisabledEvent;
import org.springframework.security.authentication.event.AuthenticationFailureExpiredEvent;
import org.springframework.security.authentication.event.AuthenticationFailureLockedEvent;
import org.springframework.security.authentication.event.AuthenticationFailureProviderNotFoundEvent;
import org.springframework.security.authentication.event.AuthenticationFailureProxyUntrustedEvent;
import org.springframework.security.authentication.event.AuthenticationFailureServiceExceptionEvent;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@NativeHint(trigger = Authentication.class,
		types = {
				@TypeHint(
						types = {SecurityExpressionOperations.class, SecurityExpressionRoot.class},
						access = {TypeAccess.DECLARED_METHODS, TypeAccess.DECLARED_FIELDS}),
				@TypeHint(
						types = {
								// From DefaultAuthenticationEventPublisher
								BadCredentialsException.class, AuthenticationFailureBadCredentialsEvent.class,
								UsernameNotFoundException.class,
								AccountExpiredException.class, AuthenticationFailureExpiredEvent.class,
								ProviderNotFoundException.class, AuthenticationFailureProviderNotFoundEvent.class,
								DisabledException.class, AuthenticationFailureDisabledEvent.class,
								LockedException.class, AuthenticationFailureLockedEvent.class,
								AuthenticationServiceException.class, AuthenticationFailureServiceExceptionEvent.class,
								CredentialsExpiredException.class, AuthenticationFailureCredentialsExpiredEvent.class,
								AuthenticationFailureProxyUntrustedEvent.class,
						},
						typeNames = {
								"org.springframework.security.authentication.cas.ProxyUntrustedException",
						}
				)
		},
		resources = @ResourceHint(patterns = "org.springframework.security.messages", isBundle = true)
)
public class CoreSecurityHints implements NativeConfiguration {
}
