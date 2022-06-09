/*
 * Copyright 2022 the original author or authors.
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
package com.example.webflux;

import java.util.Arrays;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

/**
 * @author Christoph Strobl
 */
public class RuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(org.springframework.aot.hint.RuntimeHints hints, ClassLoader classLoader) {

		// embedded db support
		hints.resources().registerPattern("schema.sql").registerPattern("data.sql");
		hints.reflection().registerTypes(Arrays.asList(TypeReference.of("org.springframework.jdbc.datasource.embedded.EmbeddedDatabase"),
				TypeReference.of("org.springframework.jdbc.support.JdbcAccessor"),
				TypeReference.of("org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory$EmbeddedDataSourceProxy"),
				TypeReference.of("org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory.EmbeddedDataSourceProxy")
		), builder -> builder.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
				MemberCategory.INVOKE_DECLARED_METHODS, MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.DECLARED_FIELDS, MemberCategory.DECLARED_CLASSES));
	}
}
