package org.springframework.web.servlet.config.annotation;

import java.util.List;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.graalvm.substitutions.FunctionalMode;
import org.springframework.graalvm.substitutions.OnlyIfPresent;
import org.springframework.graalvm.substitutions.SpringFuIsAround;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

// Configure that directly on Spring Fu side since that makes sense on JVM as well
@TargetClass(className = "org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport", onlyWith = { SpringFuIsAround.class, FunctionalMode.class, OnlyIfPresent.class })
final class Target_WebMvcConfigurationSupport {

	@Substitute
	protected final void addDefaultHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers, ContentNegotiationManager mvcContentNegotiationManager) {
		exceptionResolvers.add(new DefaultHandlerExceptionResolver());
	}
}
