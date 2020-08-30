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
import com.mysql.cj.protocol.AsyncSocketFactory;
import com.mysql.cj.protocol.NamedPipeSocketFactory;
import com.mysql.cj.protocol.SocksProxySocketFactory;
import com.mysql.cj.protocol.StandardSocketFactory;

import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.ResourcesInfo;
import org.springframework.graalvm.extension.TypeInfo;

@NativeImageHint(trigger= Driver.class, typeInfos= {
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
				StandardSocketFactory.class }
		),
}, resourcesInfos = {
		@ResourcesInfo(patterns = "com/mysql/cj/TlsSettings.properties"),
		@ResourcesInfo(isBundle = true, patterns = "com.mysql.cj.LocalizedErrorMessages")
})
public class MySqlHints implements NativeImageConfiguration {
}
