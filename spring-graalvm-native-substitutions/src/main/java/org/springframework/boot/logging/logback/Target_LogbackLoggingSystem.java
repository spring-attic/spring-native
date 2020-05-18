package org.springframework.boot.logging.logback;

import java.net.URL;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.joran.spi.JoranException;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.graalvm.substitutions.LogbackIsAround;
import org.springframework.graalvm.substitutions.OnlyPresent;
import org.springframework.graalvm.substitutions.RemoveXmlSupport;

@TargetClass(className = "org.springframework.boot.logging.logback.LogbackLoggingSystem", onlyWith = { OnlyPresent.class, LogbackIsAround.class, RemoveXmlSupport.class })
final class Target_LogbackLoggingSystem {

	@Substitute
	private void configureByResourceUrl(LoggingInitializationContext initializationContext, LoggerContext loggerContext, URL url) throws JoranException {
		throw new UnsupportedOperationException("XML support disabled");
	}
}
