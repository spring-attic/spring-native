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

package org.hibernate;

import org.hibernate.query.Query;

import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.TypeHint;

import static org.springframework.nativex.hint.AccessBits.*;

@NativeHint(trigger = org.hibernate.Session.class,
		jdkProxies = {
			@JdkProxyHint(types = {
					org.hibernate.Session.class,
					org.springframework.orm.jpa.EntityManagerProxy.class
			}),
			@JdkProxyHint(types = {
					org.hibernate.SessionFactory.class,
					org.springframework.orm.jpa.EntityManagerFactoryInfo.class
			}),
			@JdkProxyHint(types = {
					org.hibernate.jpa.HibernateEntityManagerFactory.class,
					org.springframework.orm.jpa.EntityManagerFactoryInfo.class
			}),
			@JdkProxyHint(types = {Query.class, org.hibernate.query.spi.QueryImplementor.class,})
		},
		types = {
			@TypeHint(types = org.hibernate.query.spi.QueryImplementor.class, access = PUBLIC_METHODS | DECLARED_FIELDS | DECLARED_METHODS | DECLARED_CONSTRUCTORS)
		},
		initialization = @InitializationHint(types = {
				org.hibernate.EntityMode.class,
				javax.persistence.FetchType.class,
				javax.persistence.PersistenceContextType.class,
				javax.persistence.SynchronizationType.class
		}, initTime = InitializationTime.BUILD)
)
public class HibernateHints implements NativeConfiguration {
}
