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
package org.springframework.boot.autoconfigure.r2dbc;

import java.sql.Statement;

import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.FluentR2dbcOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.ReactiveDataAccessStrategy;
import org.springframework.data.r2dbc.core.ReactiveDeleteOperation;
import org.springframework.data.r2dbc.core.ReactiveInsertOperation;
import org.springframework.data.r2dbc.core.ReactiveSelectOperation;
import org.springframework.data.r2dbc.core.ReactiveUpdateOperation;
import org.springframework.data.r2dbc.mapping.event.AfterConvertCallback;
import org.springframework.data.r2dbc.mapping.event.AfterSaveCallback;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import org.springframework.data.r2dbc.mapping.event.BeforeSaveCallback;
import org.springframework.nativex.extension.NativeConfiguration;
import org.springframework.nativex.extension.NativeHint;
import org.springframework.nativex.extension.ResourcesInfo;
import org.springframework.nativex.extension.TypeInfo;
import org.springframework.nativex.type.AccessBits;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;

import io.r2dbc.pool.ConnectionPool;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// TODO there is duplication across this hint and JDBCHints - refactor
@NativeHint(trigger=R2dbcAutoConfiguration.class, typeInfos= {
		@TypeInfo(types = {Statement.class,Statement[].class}),
		@TypeInfo(types= EmbeddedDatabase.class,typeNames="org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory$EmbeddedDataSourceProxy",
				access= AccessBits.CLASS|AccessBits.DECLARED_CONSTRUCTORS|AccessBits.DECLARED_METHODS),
		@TypeInfo(typeNames= "org.springframework.boot.autoconfigure.jdbc.DataSourceInitializerPostProcessor",access=AccessBits.FULL_REFLECTION),
		@TypeInfo(types = {
				R2dbcConverter.class, FluentR2dbcOperations.class, R2dbcEntityOperations.class,
				ReactiveDataAccessStrategy.class, ReactiveDeleteOperation.class, ReactiveInsertOperation.class,
				ReactiveSelectOperation.class, ReactiveUpdateOperation.class, AfterConvertCallback.class,
				AfterSaveCallback.class, BeforeConvertCallback.class, BeforeSaveCallback.class
		},access=AccessBits.LOAD_AND_CONSTRUCT|AccessBits.DECLARED_METHODS)
		})
@NativeHint(trigger=R2dbcAutoConfiguration.class,
		resourcesInfos = {
				@ResourcesInfo(patterns="META-INF/services/io.r2dbc.spi.ConnectionFactoryProvider"),
		},
		typeInfos= {
		@TypeInfo(typeNames = {
				// TODO review org.springframework.data.r2dbc.dialect.DialectResolver - there is some 
				// factory loading in there.
				"org.springframework.data.r2dbc.dialect.DialectResolver$BuiltInDialectProvider"
		}, types = {
				// Can't find it now but there was some form of wrapper list in R2DBC that listed this plus others
				Mono.class,
				Flux.class
		}, access = AccessBits.DECLARED_CONSTRUCTORS),
		// Enables 'dispose' method to be found
		@TypeInfo(types= ConnectionPool.class,access=AccessBits.DECLARED_METHODS)
})
public class R2dbcHints implements NativeConfiguration {
}
