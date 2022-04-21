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

package org.mariadb;

import org.mariadb.jdbc.Configuration;
import org.mariadb.jdbc.Driver;
import org.springframework.nativex.hint.FieldHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = Driver.class, types = {
		@TypeHint(types = Configuration.Builder.class, fields = {
				// Parameters and categories are explained here:
				// https://mariadb.com/kb/en/about-mariadb-connector-j/

				// Essential parameters
				@FieldHint(name = "user"),
				@FieldHint(name = "password"),
				@FieldHint(name = "rewriteBatchedStatements"),
				@FieldHint(name = "connectTimeout"),
				@FieldHint(name = "useServerPrepStmts"),
				@FieldHint(name = "useBatchMultiSend"),
				@FieldHint(name = "allowLocalInfile"),
				@FieldHint(name = "useMysqlMetadata"),

				// TLS parameters
				@FieldHint(name = "useSsl"),
				@FieldHint(name = "trustServerCertificate"),
				@FieldHint(name = "serverSslCert"),
				@FieldHint(name = "keyStore"),
				@FieldHint(name = "keyStorePassword"),
				@FieldHint(name = "keyPassword"),
				@FieldHint(name = "trustStore"),
				@FieldHint(name = "trustStorePassword"),
				@FieldHint(name = "enabledSslProtocolSuites"),
				@FieldHint(name = "enabledSslCipherSuites"),
				@FieldHint(name = "disableSslHostnameVerification"),
				@FieldHint(name = "keyStoreType"),
				@FieldHint(name = "trustStoreType"),

				// Pool parameters
				@FieldHint(name = "pool"),
				@FieldHint(name = "poolName"),
				@FieldHint(name = "maxPoolSize"),
				@FieldHint(name = "minPoolSize"),
				@FieldHint(name = "poolValidMinDelay"),
				@FieldHint(name = "maxIdleTime"),
				@FieldHint(name = "staticGlobal"),
				@FieldHint(name = "useResetConnection"),
				@FieldHint(name = "registerJmxPool"),

				// Log parameters
				@FieldHint(name = "log"),
				@FieldHint(name = "maxQuerySizeToLog"),
				@FieldHint(name = "slowQueryThresholdNanos"),
				@FieldHint(name = "profileSql"),

				// Infrequent used parameters
				@FieldHint(name = "passwordCharacterEncoding"),
				@FieldHint(name = "useFractionalSeconds"),
				@FieldHint(name = "allowMultiQueries"),
				@FieldHint(name = "dumpQueriesOnException"),
				@FieldHint(name = "useCompression"),
				@FieldHint(name = "socketFactory"),
				@FieldHint(name = "tcpNoDelay"),
				@FieldHint(name = "tcpKeepAlive"),
				@FieldHint(name = "tcpAbortiveClose"),
				@FieldHint(name = "tcpRcvBuf"),
				@FieldHint(name = "tcpSndBuf"),
				@FieldHint(name = "pipe"),
				@FieldHint(name = "tinyInt1isBit"),
				@FieldHint(name = "yearIsDateType"),
				@FieldHint(name = "sessionVariables"),
				@FieldHint(name = "localSocket"),
				@FieldHint(name = "sharedMemory"),
				@FieldHint(name = "localSocketAddress"),
				@FieldHint(name = "socketTimeout"),
				@FieldHint(name = "interactiveClient"),
				@FieldHint(name = "useOldAliasMetadataBehavior"),
				@FieldHint(name = "createDatabaseIfNotExist"),
				@FieldHint(name = "serverTimezone"),
				@FieldHint(name = "cachePrepStmts"),
				@FieldHint(name = "prepStmtCacheSize"),
				@FieldHint(name = "prepStmtCacheSqlLimit"),
				@FieldHint(name = "jdbcCompliantTruncation"),
				@FieldHint(name = "cacheCallableStmts"),
				@FieldHint(name = "callableStmtCacheSize"),
				@FieldHint(name = "useBatchMultiSendNumber"),
				@FieldHint(name = "connectionAttributes"),
				@FieldHint(name = "usePipelineAuth"),
				@FieldHint(name = "enablePacketDebug"),
				@FieldHint(name = "useBulkStmts"),
				@FieldHint(name = "autocommit"),
				@FieldHint(name = "galeraAllowedState"),
				@FieldHint(name = "includeInnodbStatusInDeadlockExceptions"),
				@FieldHint(name = "includeThreadDumpInDeadlockExceptions"),
				@FieldHint(name = "useReadAheadInput"),
				@FieldHint(name = "servicePrincipalName"),
				@FieldHint(name = "useMysqlMetadata"),
				@FieldHint(name = "defaultFetchSize"),
				@FieldHint(name = "blankTableNameMeta"),
				@FieldHint(name = "serverRsaPublicKeyFile"),
				@FieldHint(name = "allowPublicKeyRetrieval"),
				@FieldHint(name = "tlsSocketType"),
				@FieldHint(name = "credentialType"),
				@FieldHint(name = "trackSchema"),

				// Failover and Load Balancing Parameters
				@FieldHint(name = "autoReconnect"),
				@FieldHint(name = "retriesAllDown"),
				@FieldHint(name = "failoverLoopRetries"),
				@FieldHint(name = "validConnectionTimeout"),
				@FieldHint(name = "loadBalanceBlacklistTimeout"),
				@FieldHint(name = "assureReadOnly"),
				@FieldHint(name = "allowMasterDownConnection")
		})
}, resources = {
		@ResourceHint(patterns = "mariadb.properties")
})
public class MariaDbHints implements NativeConfiguration {
}
