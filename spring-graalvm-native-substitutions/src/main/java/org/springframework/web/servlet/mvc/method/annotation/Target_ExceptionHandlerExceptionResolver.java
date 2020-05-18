package org.springframework.web.servlet.mvc.method.annotation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.graalvm.substitutions.FormHttpMessageConverterIsAround;
import org.springframework.graalvm.substitutions.OnlyPresent;
import org.springframework.graalvm.substitutions.RemoveXmlSupport;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.method.ControllerAdviceBean;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;

@TargetClass(className = "org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver", onlyWith = { OnlyPresent.class, FormHttpMessageConverterIsAround.class, RemoveXmlSupport.class })
final class Target_ExceptionHandlerExceptionResolver {

	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.NewInstance, declClass = ArrayList.class)
	private ContentNegotiationManager contentNegotiationManager;

	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.NewInstance, declClass = ArrayList.class)
	private List<Object> responseBodyAdvice;

	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.NewInstance, declClass = ConcurrentHashMap.class)
	private Map<Class<?>, ExceptionHandlerMethodResolver> exceptionHandlerCache;

	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.NewInstance, declClass = LinkedHashMap.class)
	private Map<ControllerAdviceBean, ExceptionHandlerMethodResolver> exceptionHandlerAdviceCache;

	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.NewInstance, declClass = Log.class)
	protected Log logger;

	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.NewInstance, declClass = ArrayList.class)
	private List<HttpMessageConverter<?>> messageConverters;

	@Substitute
	public Target_ExceptionHandlerExceptionResolver() {
		this.messageConverters = new ArrayList<>();
		this.messageConverters.add(new ByteArrayHttpMessageConverter());
		this.messageConverters.add(new StringHttpMessageConverter());
		this.messageConverters.add(new FormHttpMessageConverter()); // Impossible to create a substitution for AllEncompassingFormHttpMessageConverter for now so we use that one
		this.contentNegotiationManager = new ContentNegotiationManager();
		this.responseBodyAdvice = new ArrayList<>();
		this.exceptionHandlerCache = new ConcurrentHashMap<>(64);
		this.exceptionHandlerAdviceCache = new LinkedHashMap<>();
		this.logger = LogFactory.getLog(getClass());
	}
}
