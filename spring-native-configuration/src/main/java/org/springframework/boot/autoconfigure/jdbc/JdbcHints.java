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

package org.springframework.boot.autoconfigure.jdbc;

import java.sql.DatabaseMetaData;
import java.sql.Statement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.util.ConcurrentBag.IConcurrentBagEntry;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeResourcesEntry;
import org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration.Hikari;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.nativex.AotOptions;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.MethodHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger=EmbeddedDataSourceConfiguration.class, types = {
		@TypeHint(types= {EmbeddedDatabase.class, JdbcAccessor.class}, typeNames="org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory$EmbeddedDataSourceProxy",
				access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS})})

@NativeHint(trigger=Hikari.class, types = {
		@TypeHint(types=DatabaseMetaData.class, methods= @MethodHint(name="getDatabaseProductName")),
		@TypeHint(types= {IConcurrentBagEntry[].class,IConcurrentBagEntry.class, Statement.class, Statement[].class}),
		@TypeHint(types = HikariDataSource.class),
	@TypeHint(types = HikariConfig.class, typeNames = "com.zaxxer.hikari.HikariConfigMXBean", access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS, TypeAccess.PUBLIC_METHODS})})
@NativeHint(trigger=DataSourceAutoConfiguration.class, resources = {
		@ResourceHint(patterns = { "schema.sql","data.sql" })
})
public class JdbcHints implements NativeConfiguration {
	@Override
	public void computeHints(NativeConfigurationRegistry registry, AotOptions aotOptions) {
		if (!aotOptions.isRemoveXmlSupport()) {
			registry.resources().add(NativeResourcesEntry.of("org/springframework/jdbc/support/sql-error-codes.xml"));
		}
	}
}
