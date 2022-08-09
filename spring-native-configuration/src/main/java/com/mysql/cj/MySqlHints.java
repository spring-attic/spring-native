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

package com.mysql.cj;

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
import com.mysql.cj.protocol.NamedPipeSocketFactory;
import com.mysql.cj.protocol.SocksProxySocketFactory;
import com.mysql.cj.protocol.StandardSocketFactory;

import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = Driver.class, types = {
		@TypeHint(types = {
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
				FeatureNotAvailableException.class,
				InvalidConnectionAttributeException.class,
				OperationCancelledException.class,
				PasswordExpiredException.class,
				PropertyNotModifiableException.class,
				RSAException.class,
				SSLParamsException.class,
				StatementIsClosedException.class,
				UnableToConnectException.class,
				UnsupportedConnectionStringException.class,
				WrongArgumentException.class,
				NamedPipeSocketFactory.class,
				SocksProxySocketFactory.class,
				StandardSocketFactory.class,
				PerConnectionLRUFactory.class
		}),
		@TypeHint(types= {
				DeadlockTimeoutRollbackMarker.class,
				NumberOutOfRange.class,
				StreamingNotifiable.class,
				Driver.class,
				NdbLoadBalanceExceptionChecker.class,
				StandardLoadBalanceExceptionChecker.class,
				StandardLogger.class,
				}
		),
}, resources = {
		@ResourceHint(patterns = "com/mysql/cj/TlsSettings.properties"),
		@ResourceHint(isBundle = true, patterns = "com.mysql.cj.LocalizedErrorMessages")
})
public class MySqlHints implements NativeConfiguration {
}
