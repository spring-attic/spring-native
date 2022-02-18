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

package org.springframework.boot.autoconfigure.data.jdbc;

import org.springframework.data.jdbc.core.convert.RelationResolver;
import org.springframework.data.jdbc.repository.config.JdbcRepositoryConfigExtension;
import org.springframework.data.jdbc.repository.support.JdbcRepositoryFactoryBean;
import org.springframework.data.jdbc.repository.support.SimpleJdbcRepository;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.TypeHint;

/**
 * @author Christoph Strobl
 */
@NativeHint(trigger = JdbcRepositoryFactoryBean.class,
		types = {
		@TypeHint(types = {
						JdbcRepositoryFactoryBean.class,
						JdbcRepositoryConfigExtension.class,
						RelationResolver.class
				}),
				@TypeHint(types = SimpleJdbcRepository.class, access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.PUBLIC_METHODS})
		}, jdkProxies = @JdkProxyHint(typeNames = {
				"org.springframework.data.jdbc.core.convert.RelationResolver",
				"org.springframework.aop.SpringProxy",
				"org.springframework.aop.framework.Advised",
				"org.springframework.core.DecoratingProxy"
		})
)
public class JdbcRepositoriesHints implements NativeConfiguration {

}
