/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.boot.autoconfigure.data.r2dbc;

import org.springframework.data.r2dbc.repository.support.R2dbcRepositoryFactoryBean;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.graal.extension.NativeImageHint;
import org.springframework.graal.extension.TypeInfo;
import org.springframework.graal.type.AccessBits;

@NativeImageHint(trigger=R2dbcRepositoriesAutoConfiguration.class, typeInfos = {
	@TypeInfo(types= {
			SimpleR2dbcRepository.class,
			R2dbcRepositoryFactoryBean.class
		},access=AccessBits.DECLARED_FIELDS|AccessBits.DECLARED_METHODS|AccessBits.DECLARED_CONSTRUCTORS)
	})
public class Hints implements NativeImageConfiguration {
}
