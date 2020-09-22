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
package org.springframework.boot.autoconfigure.jdbc;

import java.sql.DatabaseMetaData;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.util.ConcurrentBag.IConcurrentBagEntry;

import org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration.Hikari;
import org.springframework.graalvm.extension.MethodInfo;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.ResourcesInfo;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.support.ConfigOptions;
import org.springframework.graalvm.type.AccessBits;
import org.springframework.graalvm.type.CompilationHint;
import org.springframework.graalvm.type.ResourcesDescriptor;
import org.springframework.graalvm.type.TypeSystem;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.support.JdbcAccessor;

@NativeImageHint(trigger=DataSourceInitializationConfiguration.Registrar.class, typeInfos= {
		@TypeInfo(types=DataSourceInitializerPostProcessor.class, access=AccessBits.FULL_REFLECTION)})

@NativeImageHint(trigger=EmbeddedDataSourceConfiguration.class, typeInfos= {
		@TypeInfo(types= {EmbeddedDatabase.class, JdbcAccessor.class}, typeNames="org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory$EmbeddedDataSourceProxy",
				access=AccessBits.LOAD_AND_CONSTRUCT|AccessBits.DECLARED_METHODS)})

@NativeImageHint(trigger=Hikari.class, typeInfos= {
		@TypeInfo(types=DatabaseMetaData.class,methods= {@MethodInfo(name="getDatabaseProductName")}),
		@TypeInfo(types= {HikariDataSource.class, IConcurrentBagEntry[].class,IConcurrentBagEntry.class, Statement.class, Statement[].class}
		),
	@TypeInfo(types = HikariConfig.class, typeNames = "com.zaxxer.hikari.HikariConfigMXBean", access = AccessBits.FULL_REFLECTION)})
@NativeImageHint(trigger=DataSourceAutoConfiguration.class, resourcesInfos = {
		@ResourcesInfo(patterns = {"schema.sql"})
})
public class JdbcHints implements NativeImageConfiguration {
	@Override
	public List<CompilationHint> computeHints(TypeSystem typeSystem) {
		if (!ConfigOptions.shouldRemoveXmlSupport()) {
			CompilationHint ch = new CompilationHint();
			// Referenced from org.springframework.jdbc.support.SQLErrorCodesFactory
			ResourcesDescriptor sqlErrorCodes = new ResourcesDescriptor(new String[] {"org/springframework/jdbc/support/sql-error-codes.xml"},false);
			ch.addResourcesDescriptor(sqlErrorCodes);
			return Collections.singletonList(ch);
		}
		return Collections.emptyList();
	}
}
