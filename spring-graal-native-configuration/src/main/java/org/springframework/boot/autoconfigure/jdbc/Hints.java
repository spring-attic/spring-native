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
import org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration.Hikari;
import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.graal.extension.TypeInfo;
import org.springframework.graal.type.AccessBits;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/*
proposedHints.put("Lorg/springframework/boot/autoconfigure/jdbc/DataSourceInitializationConfiguration$Registrar;",
		new CompilationHint(false, false, new String[] {
				"org.springframework.boot.autoconfigure.jdbc.DataSourceInitializerPostProcessor:EXISTENCE_CMF"
		}));

proposedHints.put("Lorg/springframework/boot/autoconfigure/jdbc/EmbeddedDataSourceConfiguration;",
		new CompilationHint(false, false, new String[] {
				"org.h2.store.fs.FilePathDisk:REGISTRAR",
				"org.h2.store.fs.FilePathMem:REGISTRAR",
				"org.h2.store.fs.FilePathMemLZF:REGISTRAR",
				"org.h2.store.fs.FilePathNioMem:REGISTRAR",
				"org.h2.store.fs.FilePathNioMemLZF:REGISTRAR",
				"org.h2.store.fs.FilePathSplit:REGISTRAR",
				"org.h2.store.fs.FilePathNio:REGISTRAR",
				"org.h2.store.fs.FilePathNioMapped:REGISTRAR",
				"org.h2.store.fs.FilePathAsync:REGISTRAR",
				"org.h2.store.fs.FilePathZip:REGISTRAR",
				"org.h2.store.fs.FilePathRetryOnInterrupt:REGISTRAR"
		})); REGISTRAR = class.forname and constructor reflection/invocation
*/
@ConfigurationHint(value=DataSourceInitializationConfiguration.Registrar.class,
	typeInfos= {@TypeInfo(types=DataSourceInitializerPostProcessor.class,access=AccessBits.FULL_REFLECTION)})
@ConfigurationHint(value=EmbeddedDataSourceConfiguration.class,typeInfos= {
		@TypeInfo(
				access=AccessBits.CLASS|AccessBits.PUBLIC_CONSTRUCTORS,
				typeNames= {"org.h2.store.fs.FilePathMemLZF","org.h2.store.fs.FilePathNioMemLZF"},
				types= {FilePathDisk.class, FilePathMem.class, FilePathNioMem.class, FilePathSplit.class,FilePathNio.class,FilePathNioMapped.class,FilePathAsync.class,FilePathZip.class,FilePathRetryOnInterrupt.class}),
		})
@ConfigurationHint(value=Hikari.class,typeInfos= {
		@TypeInfo(types= {HikariDataSource.class,HikariConfig.class,MVTableEngine.class})
	})
public class Hints implements NativeImageConfiguration {
}
