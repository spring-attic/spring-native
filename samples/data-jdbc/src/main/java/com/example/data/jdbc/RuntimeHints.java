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
package com.example.data.jdbc;

import java.util.Arrays;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.context.annotation.AutoProxyRegistrar;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.ProxyTransactionManagementConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Christoph Strobl
 */
public class RuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(org.springframework.aot.hint.RuntimeHints hints, ClassLoader classLoader) {

		// Transactions
		hints.proxies().registerJdkProxy(TypeReference.of(Transactional.class),
				TypeReference.of("org.springframework.core.annotation.SynthesizedAnnotation"));
		hints.reflection().registerType(TypeReference.of(Transactional.class),
				builder -> builder.withMembers(MemberCategory.PUBLIC_FIELDS, MemberCategory.DECLARED_CLASSES,
						MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_DECLARED_METHODS,
						MemberCategory.DECLARED_FIELDS, MemberCategory.INVOKE_DECLARED_METHODS,
						MemberCategory.INVOKE_DECLARED_CONSTRUCTORS));
		hints.reflection()
				.registerTypes(Arrays.asList(TypeReference.of(Propagation.class),
						TypeReference.of(org.springframework.transaction.annotation.Isolation.class),
						TypeReference.of(org.springframework.transaction.annotation.Propagation.class),
						TypeReference.of(org.springframework.jdbc.datasource.ConnectionProxy.class),
						TypeReference.of(org.springframework.jdbc.support.JdbcAccessor.class),
						TypeReference.of(org.springframework.jdbc.support.JdbcTransactionManager.class),
						TypeReference.of(TransactionDefinition.class)

				), builder -> builder.withMembers(MemberCategory.PUBLIC_FIELDS, MemberCategory.DECLARED_CLASSES,
						MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_DECLARED_METHODS,
						MemberCategory.DECLARED_FIELDS, MemberCategory.INVOKE_DECLARED_METHODS,
						MemberCategory.INVOKE_DECLARED_CONSTRUCTORS));

		hints.reflection().registerTypes(
				Arrays.asList(TypeReference.of(AutoProxyRegistrar.class),
						TypeReference.of(ProxyTransactionManagementConfiguration.class),
						TypeReference
								.of("org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor$1")),
				builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS,
						MemberCategory.INVOKE_DECLARED_CONSTRUCTORS));

		// Embedded Database
		hints.resources().registerPattern("schema.sql").registerPattern("data.sql");
		hints.reflection().registerTypes(Arrays.asList(TypeReference.of("org.springframework.jdbc.datasource.embedded.EmbeddedDatabase"),
				TypeReference.of("org.springframework.jdbc.support.JdbcAccessor"),
				TypeReference.of("org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory$EmbeddedDataSourceProxy"),
				TypeReference.of("org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory.EmbeddedDataSourceProxy")
		), builder -> builder.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
				MemberCategory.INVOKE_DECLARED_METHODS, MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.DECLARED_FIELDS, MemberCategory.DECLARED_CLASSES));

		hints.resources().registerPattern("db/h2/schema.sql");

		// H2
		hints.reflection()
				.registerTypes(Arrays.asList(TypeReference.of("org.h2.store.fs.async.FilePathAsync"),
								TypeReference.of("org.h2.store.fs.disk.FilePathDisk"), TypeReference.of("org.h2.store.fs.mem.FilePathMem"),
								TypeReference.of("org.h2.store.fs.niomapped.FilePathNioMapped"),
								TypeReference.of("org.h2.store.fs.niomem.FilePathNioMem"),
								TypeReference.of("org.h2.store.fs.retry.FilePathRetryOnInterrupt"),
								TypeReference.of("org.h2.store.fs.split.FilePathSplit"),
								TypeReference.of("org.h2.store.fs.zip.FilePathZip"), TypeReference.of("org.h2.store.fs.mem.FilePathMemLZF"),
								TypeReference.of("org.h2.store.fs.niomem.FilePathNioMemLZF")),
						builder -> builder.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
								MemberCategory.INVOKE_DECLARED_METHODS, MemberCategory.INVOKE_PUBLIC_METHODS));

		hints.resources().registerPattern("org/h2/util/data.zip");
	}
}
