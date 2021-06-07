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
import org.springframework.data.relational.core.mapping.event.AfterDeleteCallback;
import org.springframework.data.relational.core.mapping.event.AfterLoadCallback;
import org.springframework.data.relational.core.mapping.event.AfterSaveCallback;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;
import org.springframework.data.relational.core.mapping.event.BeforeDeleteCallback;
import org.springframework.data.relational.core.mapping.event.BeforeSaveCallback;
import org.springframework.data.relational.core.mapping.event.RelationalAuditingCallback;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.TypeHint;

/**
 * @author Christoph Strobl
 */
@NativeHint(trigger = JdbcRepositoriesAutoConfiguration.class, //
		types = @TypeHint(types = {
				JdbcRepositoryFactoryBean.class,
				JdbcRepositoryConfigExtension.class,
				SimpleJdbcRepository.class,
				RelationResolver.class,
				AfterDeleteCallback.class,
				AfterLoadCallback.class,
				AfterSaveCallback.class,
				BeforeConvertCallback.class,
				BeforeDeleteCallback.class,
				BeforeSaveCallback.class,
				RelationalAuditingCallback.class
		}), jdkProxies = @JdkProxyHint(typeNames = {
				"org.springframework.data.jdbc.core.convert.RelationResolver",
				"org.springframework.aop.SpringProxy",
				"org.springframework.aop.framework.Advised",
				"org.springframework.core.DecoratingProxy"
		})
)
public class JdbcRepositoriesHints implements NativeConfiguration {

}
