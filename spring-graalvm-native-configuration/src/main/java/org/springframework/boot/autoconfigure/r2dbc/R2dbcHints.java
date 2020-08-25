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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Statement;

import org.h2.mvstore.db.MVTableEngine;
import org.h2.store.fs.FilePathAsync;
import org.h2.store.fs.FilePathDisk;
import org.h2.store.fs.FilePathMem;
import org.h2.store.fs.FilePathNio;
import org.h2.store.fs.FilePathNioMapped;
import org.h2.store.fs.FilePathNioMem;
import org.h2.store.fs.FilePathRetryOnInterrupt;
import org.h2.store.fs.FilePathSplit;
import org.h2.store.fs.FilePathZip;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.ResourcesInfo;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;

import io.r2dbc.pool.ConnectionPool;

// TODO there is duplication across this hint and JDBCHints - refactor
@NativeImageHint(trigger=R2dbcAutoConfiguration.class, typeInfos= {
		@TypeInfo(types = {Statement.class,Statement[].class}),
		@TypeInfo(types= EmbeddedDatabase.class,typeNames="org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory$EmbeddedDataSourceProxy",
				access= AccessBits.CLASS|AccessBits.DECLARED_CONSTRUCTORS|AccessBits.DECLARED_METHODS),
		@TypeInfo(typeNames= "org.springframework.boot.autoconfigure.jdbc.DataSourceInitializerPostProcessor",access=AccessBits.FULL_REFLECTION)
		})
@NativeImageHint(trigger=R2dbcAutoConfiguration.class, 
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
public class R2dbcHints implements NativeImageConfiguration {
}
