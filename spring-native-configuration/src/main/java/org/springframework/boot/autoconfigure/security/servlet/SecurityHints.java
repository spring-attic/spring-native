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

import java.util.Collections;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.http.HttpServletRequestWrapper;

import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.type.AccessDescriptor;
import org.springframework.nativex.type.HintDeclaration;
import org.springframework.nativex.type.TypeSystem;
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
import org.springframework.security.web.access.expression.WebSecurityExpressionRoot;

@NativeHint(trigger=SecurityAutoConfiguration.class, types = {
		@TypeHint(
				types= {SecurityExpressionOperations.class,SecurityExpressionRoot.class,WebSecurityExpressionRoot.class},
				access=AccessBits.CLASS|AccessBits.DECLARED_METHODS|AccessBits.DECLARED_FIELDS),
		@TypeHint(types= {
				// From DefaultAuthenticationEventPublisher
				BadCredentialsException.class,AuthenticationFailureBadCredentialsEvent.class,
				UsernameNotFoundException.class,
				AccountExpiredException.class,AuthenticationFailureExpiredEvent.class,
				ProviderNotFoundException.class,AuthenticationFailureProviderNotFoundEvent.class,
				DisabledException.class,AuthenticationFailureDisabledEvent.class,
				LockedException.class,AuthenticationFailureLockedEvent.class,
				AuthenticationServiceException.class,AuthenticationFailureServiceExceptionEvent.class,
				CredentialsExpiredException.class,AuthenticationFailureCredentialsExpiredEvent.class,
				AuthenticationFailureProxyUntrustedEvent.class,
				// See comment below about RestrictedRequestWrapper
				},
				typeNames= {
						"org.springframework.security.authentication.cas.ProxyUntrustedException",
				}
		),
		// TODO interesting that gs-securing-web causes these to be needed although it is in thymeleaf (due to SpEL expressions I think)
		@TypeHint(
			typeNames = "org.thymeleaf.standard.expression.RestrictedRequestAccessUtils$RestrictedRequestWrapper",
			types= { HttpServletRequestWrapper.class,ServletRequestWrapper.class,ServletRequest.class},
			access=AccessBits.CLASS|AccessBits.DECLARED_CONSTRUCTORS|AccessBits.DECLARED_FIELDS|AccessBits.DECLARED_METHODS),

		@TypeHint(typeNames = {
				"org.springframework.boot.autoconfigure.security.DefaultWebSecurityCondition",
				"org.springframework.boot.autoconfigure.security.DefaultWebSecurityCondition$Classes",
				"org.springframework.boot.autoconfigure.security.DefaultWebSecurityCondition$Beans",
		}, access = AccessBits.ALL),
})
public class SecurityHints implements NativeConfiguration {
	@Override
	public List<HintDeclaration> computeHints(TypeSystem typeSystem) {
		boolean javaxServletFilterAround = typeSystem.resolveDotted("javax.servlet.Filter",true)!=null;
		if (javaxServletFilterAround) {
			// This class includes methods that are called via SpEL and in a return value,  nested in generics, is a reference to javax.servlet.Filter
			HintDeclaration hd = new HintDeclaration();
			hd.addDependantType("org.springframework.security.config.annotation.web.configuration.AutowiredWebSecurityConfigurersIgnoreParents",
					new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT_AND_PUBLIC_METHODS));
			return Collections.singletonList(hd);
		}
		return Collections.emptyList();
	}
}
