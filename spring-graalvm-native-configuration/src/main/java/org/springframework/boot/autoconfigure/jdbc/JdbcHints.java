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

import com.mysql.cj.conf.url.FailoverConnectionUrl;
import com.mysql.cj.conf.url.FailoverDnsSrvConnectionUrl;
import com.mysql.cj.conf.url.LoadBalanceConnectionUrl;
import com.mysql.cj.conf.url.LoadBalanceDnsSrvConnectionUrl;
import com.mysql.cj.conf.url.ReplicationConnectionUrl;
import com.mysql.cj.conf.url.ReplicationDnsSrvConnectionUrl;
import com.mysql.cj.conf.url.SingleConnectionUrl;
import com.mysql.cj.conf.url.XDevApiConnectionUrl;
import com.mysql.cj.conf.url.XDevApiDnsSrvConnectionUrl;
import com.mysql.cj.exceptions.AssertionFailedException;
import com.mysql.cj.exceptions.CJCommunicationsException;
import com.mysql.cj.exceptions.CJConnectionFeatureNotAvailableException;
import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.exceptions.CJOperationNotSupportedException;
import com.mysql.cj.exceptions.CJPacketTooBigException;
import com.mysql.cj.exceptions.CJTimeoutException;
import com.mysql.cj.exceptions.ClosedOnExpiredPasswordException;
import com.mysql.cj.exceptions.ConnectionIsClosedException;
import com.mysql.cj.exceptions.DataConversionException;
import com.mysql.cj.exceptions.DataReadException;
import com.mysql.cj.exceptions.DataTruncationException;
import com.mysql.cj.exceptions.DeadlockTimeoutRollbackMarker;
import com.mysql.cj.exceptions.FeatureNotAvailableException;
import com.mysql.cj.exceptions.InvalidConnectionAttributeException;
import com.mysql.cj.exceptions.NumberOutOfRange;
import com.mysql.cj.exceptions.OperationCancelledException;
import com.mysql.cj.exceptions.PasswordExpiredException;
import com.mysql.cj.exceptions.PropertyNotModifiableException;
import com.mysql.cj.exceptions.RSAException;
import com.mysql.cj.exceptions.SSLParamsException;
import com.mysql.cj.exceptions.StatementIsClosedException;
import com.mysql.cj.exceptions.StreamingNotifiable;
import com.mysql.cj.exceptions.UnableToConnectException;
import com.mysql.cj.exceptions.UnsupportedConnectionStringException;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.jdbc.Driver;
import com.mysql.cj.jdbc.ha.NdbLoadBalanceExceptionChecker;
import com.mysql.cj.jdbc.ha.StandardLoadBalanceExceptionChecker;
import com.mysql.cj.log.StandardLogger;
import com.mysql.cj.protocol.AsyncSocketFactory;
import com.mysql.cj.protocol.NamedPipeSocketFactory;
import com.mysql.cj.protocol.SocksProxySocketFactory;
import com.mysql.cj.protocol.StandardSocketFactory;
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

@NativeImageHint(trigger=DataSourceInitializationConfiguration.Registrar.class, typeInfos= {
		@TypeInfo(types=DataSourceInitializerPostProcessor.class, access=AccessBits.FULL_REFLECTION)})

@NativeImageHint(trigger=EmbeddedDataSourceConfiguration.class, typeInfos= {
		@TypeInfo(types=EmbeddedDatabase.class, typeNames="org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory$EmbeddedDataSourceProxy",
				access=AccessBits.LOAD_AND_CONSTRUCT|AccessBits.DECLARED_METHODS),
		@TypeInfo(
				typeNames= {"org.h2.store.fs.FilePathMemLZF","org.h2.store.fs.FilePathNioMemLZF"},
				types= {MVTableEngine.class, Statement.class, Statement[].class, FilePathDisk.class, FilePathMem.class, FilePathNioMem.class, FilePathSplit.class,FilePathNio.class,FilePathNioMapped.class,FilePathAsync.class,FilePathZip.class,FilePathRetryOnInterrupt.class}
		)})

@NativeImageHint(trigger=Hikari.class, typeInfos= {
		@TypeInfo(types= {HikariDataSource.class, IConcurrentBagEntry[].class,IConcurrentBagEntry.class}
		),
	@TypeInfo(types = HikariConfig.class, access = AccessBits.FULL_REFLECTION)})
@NativeImageHint(trigger=DataSourceAutoConfiguration.class, resourcesInfos = {
		// Referenced from org.springframework.jdbc.support.SQLErrorCodesFactory
		@ResourcesInfo(patterns = {"org/springframework/jdbc/support/sql-error-codes.xml","schema.sql"})
})
// MySQL JDBC driver
@NativeImageHint(trigger=DataSourceAutoConfiguration.class, typeInfos= {
		@TypeInfo(types= {
				FailoverConnectionUrl.class,
				FailoverDnsSrvConnectionUrl.class,
				LoadBalanceConnectionUrl.class,
				LoadBalanceDnsSrvConnectionUrl.class,
				ReplicationConnectionUrl.class,
				ReplicationDnsSrvConnectionUrl.class,
				SingleConnectionUrl.class,
				XDevApiConnectionUrl.class,
				XDevApiDnsSrvConnectionUrl.class,
				AssertionFailedException.class,
				CJCommunicationsException.class,
				CJConnectionFeatureNotAvailableException.class,
				CJException.class,
				CJOperationNotSupportedException.class,
				CJPacketTooBigException.class,
				CJTimeoutException.class,
				ClosedOnExpiredPasswordException.class,
				ConnectionIsClosedException.class,
				DataConversionException.class,
				DataReadException.class,
				DataTruncationException.class,
				DeadlockTimeoutRollbackMarker.class,
				FeatureNotAvailableException.class,
				InvalidConnectionAttributeException.class,
				NumberOutOfRange.class,
				OperationCancelledException.class,
				PasswordExpiredException.class,
				PropertyNotModifiableException.class,
				RSAException.class,
				SSLParamsException.class,
				StatementIsClosedException.class,
				StreamingNotifiable.class,
				UnableToConnectException.class,
				UnsupportedConnectionStringException.class,
				WrongArgumentException.class,
				Driver.class,
				NdbLoadBalanceExceptionChecker.class,
				StandardLoadBalanceExceptionChecker.class,
				StandardLogger.class,
				AsyncSocketFactory.class,
				NamedPipeSocketFactory.class,
				SocksProxySocketFactory.class,
				StandardSocketFactory.class },
				access=AccessBits.LOAD_AND_CONSTRUCT),
}, resourcesInfos = {
		@ResourcesInfo(isBundle = true, patterns = {
			"com.mysql.cj.LocalizedErrorMessages"
		})
})
public class JdbcHints implements NativeImageConfiguration {
}
