package ch.qos.logback.classic.util;

import java.net.URL;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.gaffer.GafferUtil;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.StatusManager;
import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.nativex.substitutions.LogbackIsAround;
import org.springframework.nativex.substitutions.OnlyIfPresent;
import org.springframework.nativex.substitutions.RemoveXmlSupport;

@TargetClass(className = "ch.qos.logback.classic.util.ContextInitializer", onlyWith = { OnlyIfPresent.class, LogbackIsAround.class, RemoveXmlSupport.class })
final class Target_ContextInitializer {

	@Alias
	LoggerContext loggerContext;

	@Substitute
	public void configureByResource(URL url) throws JoranException {
		if (url == null) {
			throw new IllegalArgumentException("URL argument cannot be null");
		}
		final String urlString = url.toString();
		if (urlString.endsWith("groovy")) {
			if (EnvUtil.isGroovyAvailable()) {
				// avoid directly referring to GafferConfigurator so as to avoid
				// loading groovy.lang.GroovyObject . See also http://jira.qos.ch/browse/LBCLASSIC-214
				GafferUtil.runGafferConfiguratorOn(loggerContext, this, url);
			} else {
				StatusManager sm = loggerContext.getStatusManager();
				sm.add(new ErrorStatus("Groovy classes are not available on the class path. ABORTING INITIALIZATION.", loggerContext));
			}
		} else {
			throw new LogbackException("Unexpected filename extension of file [" + url.toString() + "]. Should be either .groovy or .xml");
		}
	}
}
