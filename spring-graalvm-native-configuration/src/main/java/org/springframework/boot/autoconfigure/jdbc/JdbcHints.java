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
import org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration.Hikari;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.ResourcesInfo;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.util.ConcurrentBag.IConcurrentBagEntry;

@NativeImageHint(trigger=DataSourceInitializationConfiguration.Registrar.class,
	typeInfos= {@TypeInfo(types=DataSourceInitializerPostProcessor.class,access=AccessBits.FULL_REFLECTION)})
@NativeImageHint(trigger=EmbeddedDataSourceConfiguration.class, typeInfos= {
		@TypeInfo(types=EmbeddedDatabase.class,typeNames="org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory$EmbeddedDataSourceProxy",
				access=AccessBits.CLASS|AccessBits.DECLARED_CONSTRUCTORS|AccessBits.DECLARED_METHODS),
		@TypeInfo(
				access=AccessBits.CLASS|AccessBits.DECLARED_CONSTRUCTORS,
				typeNames= {
						"org.h2.store.fs.FilePathMemLZF","org.h2.store.fs.FilePathNioMemLZF"},
				types= {
						FilePathDisk.class, FilePathMem.class, FilePathNioMem.class, FilePathSplit.class,FilePathNio.class,FilePathNioMapped.class,FilePathAsync.class,FilePathZip.class,FilePathRetryOnInterrupt.class}),
})
@NativeImageHint(trigger=Hikari.class,typeInfos= {
		@TypeInfo(types= {HikariDataSource.class,HikariConfig.class,MVTableEngine.class,Statement.class,Statement[].class,
				// TODO what is the right place for this one? (and its array variant)
				IConcurrentBagEntry[].class,IConcurrentBagEntry.class})
	})
// MySQL JDBC driver
@NativeImageHint(trigger=DataSourceAutoConfiguration.class, typeInfos= {
		@TypeInfo(
				access=AccessBits.CLASS|AccessBits.DECLARED_CONSTRUCTORS,
				types= {com.mysql.cj.conf.url.FailoverConnectionUrl.class,
						com.mysql.cj.conf.url.FailoverDnsSrvConnectionUrl.class,
						com.mysql.cj.conf.url.LoadBalanceConnectionUrl.class,
						com.mysql.cj.conf.url.LoadBalanceDnsSrvConnectionUrl.class,
						com.mysql.cj.conf.url.ReplicationConnectionUrl.class,
						com.mysql.cj.conf.url.ReplicationDnsSrvConnectionUrl.class,
						com.mysql.cj.conf.url.SingleConnectionUrl.class,
						com.mysql.cj.conf.url.XDevApiConnectionUrl.class,
						com.mysql.cj.conf.url.XDevApiDnsSrvConnectionUrl.class,
						com.mysql.cj.exceptions.AssertionFailedException.class,
						com.mysql.cj.exceptions.CJCommunicationsException.class,
						com.mysql.cj.exceptions.CJConnectionFeatureNotAvailableException.class,
						com.mysql.cj.exceptions.CJException.class,
						com.mysql.cj.exceptions.CJOperationNotSupportedException.class,
						com.mysql.cj.exceptions.CJPacketTooBigException.class,
						com.mysql.cj.exceptions.CJTimeoutException.class,
						com.mysql.cj.exceptions.ClosedOnExpiredPasswordException.class,
						com.mysql.cj.exceptions.ConnectionIsClosedException.class,
						com.mysql.cj.exceptions.DataConversionException.class,
						com.mysql.cj.exceptions.DataReadException.class,
						com.mysql.cj.exceptions.DataTruncationException.class,
						com.mysql.cj.exceptions.DeadlockTimeoutRollbackMarker.class,
						com.mysql.cj.exceptions.FeatureNotAvailableException.class,
						com.mysql.cj.exceptions.InvalidConnectionAttributeException.class,
						com.mysql.cj.exceptions.NumberOutOfRange.class,
						com.mysql.cj.exceptions.OperationCancelledException.class,
						com.mysql.cj.exceptions.PasswordExpiredException.class,
						com.mysql.cj.exceptions.PropertyNotModifiableException.class,
						com.mysql.cj.exceptions.RSAException.class,
						com.mysql.cj.exceptions.SSLParamsException.class,
						com.mysql.cj.exceptions.StatementIsClosedException.class,
						com.mysql.cj.exceptions.StreamingNotifiable.class,
						com.mysql.cj.exceptions.UnableToConnectException.class,
						com.mysql.cj.exceptions.UnsupportedConnectionStringException.class,
						com.mysql.cj.exceptions.WrongArgumentException.class,
						com.mysql.cj.jdbc.Driver.class,
						com.mysql.cj.jdbc.ha.NdbLoadBalanceExceptionChecker.class,
						com.mysql.cj.jdbc.ha.StandardLoadBalanceExceptionChecker.class,
						com.mysql.cj.log.StandardLogger.class,
						com.mysql.cj.protocol.AsyncSocketFactory.class,
						com.mysql.cj.protocol.NamedPipeSocketFactory.class,
						com.mysql.cj.protocol.SocksProxySocketFactory.class,
						com.mysql.cj.protocol.StandardSocketFactory.class}),
}, resourcesInfos = {
		@ResourcesInfo(isBundle = true, patterns = {
			"com.mysql.cj.LocalizedErrorMessages"
		})
})
public class JdbcHints implements NativeImageConfiguration {
}
