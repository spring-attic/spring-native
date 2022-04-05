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

package org.springframework.session;

import org.springframework.nativex.hint.SerializationHint;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;

import java.util.ArrayList;
import java.util.TreeSet;

@SerializationHint(types = {
        String.class,
        ArrayList.class,
        TreeSet.class,
        SecurityContextImpl.class,
        SimpleGrantedAuthority.class,
        User.class,
        Number.class,
        Long.class,
        Integer.class,
        AbstractAuthenticationToken.class,
        UsernamePasswordAuthenticationToken.class,
        StackTraceElement.class,
        Throwable.class,
        Exception.class,
        RuntimeException.class,
        AuthenticationException.class,
        BadCredentialsException.class,
        UsernameNotFoundException.class,
        AccountExpiredException.class,
        ProviderNotFoundException.class,
        DisabledException.class,
        LockedException.class,
        AuthenticationServiceException.class,
        CredentialsExpiredException.class,
        InsufficientAuthenticationException.class,
        SessionAuthenticationException.class,
        RememberMeAuthenticationException.class
}, typeNames = {
        "org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken",
        "org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken",
        "org.springframework.security.oauth2.client.authentication.OAuth2AuthorizationCodeAuthenticationToken",
        "org.springframework.security.oauth2.core.OAuth2AuthenticationException",
        "org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken",
        "org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken",
        "java.util.Collections$UnmodifiableCollection",
        "java.util.Collections$UnmodifiableList",
        "java.util.Collections$EmptyList",
        "java.util.Collections$UnmodifiableRandomAccessList",
        "java.util.Collections$UnmodifiableSet",
        "org.springframework.security.core.userdetails.User$AuthorityComparator"
})
public class CommonSessionSecuritySerializables {
}
