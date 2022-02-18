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

package org.springframework.boot.autoconfigure.data.r2dbc;

import org.springframework.data.r2dbc.dialect.DialectResolver.R2dbcDialectProvider;
import org.springframework.data.r2dbc.repository.config.R2dbcRepositoryConfigurationExtension;
import org.springframework.data.r2dbc.repository.support.R2dbcRepositoryFactoryBean;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;

@NativeHint(trigger = R2dbcRepositoryFactoryBean.class, types = {
		@TypeHint(types = {
				R2dbcRepositoryFactoryBean.class,
				R2dbcRepositoryConfigurationExtension.class,
				R2dbcRepositoriesAutoConfigureRegistrar.class,
				SimpleR2dbcRepository.class,
				RepositoryMetadata.class,
				R2dbcDialectProvider.class
		}, typeNames = {"org.springframework.data.r2dbc.dialect.DialectResolver.R2dbcDialectProvider.BuiltInDialectProvider"}
				, access = { TypeAccess.DECLARED_FIELDS, TypeAccess.DECLARED_METHODS, TypeAccess.DECLARED_CONSTRUCTORS }
		)
})
public class R2dbcRepositoriesHints implements NativeConfiguration {

}
