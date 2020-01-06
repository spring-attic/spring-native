package org.springframework.web.servlet.mvc.method.annotation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.method.ControllerAdviceBean;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;

// To avoid XML converters
@TargetClass(className = "org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver")
final class Target_ExceptionHandlerExceptionResolver {

	@Alias
	private ContentNegotiationManager contentNegotiationManager;

	@Alias
	private List<Object> responseBodyAdvice;

	@Alias
	private Map<Class<?>, ExceptionHandlerMethodResolver> exceptionHandlerCache;

	@Alias
	private Map<ControllerAdviceBean, ExceptionHandlerMethodResolver> exceptionHandlerAdviceCache;

	@Alias
	protected Log logger;

	@Substitute
	public Target_ExceptionHandlerExceptionResolver() {
		this.contentNegotiationManager = new ContentNegotiationManager();
		this.responseBodyAdvice = new ArrayList<>();
		this.exceptionHandlerCache = new ConcurrentHashMap<>(64);
		this.exceptionHandlerAdviceCache = new LinkedHashMap<>();
		this.logger = LogFactory.getLog(getClass());
	}

}
