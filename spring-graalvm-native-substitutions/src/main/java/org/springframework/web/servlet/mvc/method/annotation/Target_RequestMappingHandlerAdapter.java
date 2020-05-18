package org.springframework.web.servlet.mvc.method.annotation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.graalvm.substitutions.FormHttpMessageConverterIsAround;
import org.springframework.graalvm.substitutions.OnlyPresent;
import org.springframework.graalvm.substitutions.RemoveXmlSupport;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.support.DefaultSessionAttributeStore;
import org.springframework.web.bind.support.SessionAttributeStore;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.context.request.async.DeferredResultProcessingInterceptor;
import org.springframework.web.method.ControllerAdviceBean;
import org.springframework.web.method.annotation.SessionAttributesHandler;

@TargetClass(className = "org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter", onlyWith = { OnlyPresent.class, FormHttpMessageConverterIsAround.class, RemoveXmlSupport.class })
final class Target_RequestMappingHandlerAdapter {

	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.NewInstance, declClass = ArrayList.class)
	private List<HttpMessageConverter<?>> messageConverters;

	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.NewInstance, declClass = ArrayList.class)
	private List<Object> requestResponseBodyAdvice;

	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.NewInstance, declClass = Log.class)
	protected Log logger;

	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.NewInstance, declClass = ContentNegotiationManager.class)
	private ContentNegotiationManager contentNegotiationManager;

	@Alias
	private AsyncTaskExecutor taskExecutor;

	@Alias
	private CallableProcessingInterceptor[] callableInterceptors;

	@Alias
	private DeferredResultProcessingInterceptor[] deferredResultInterceptors;

	@Alias
	private ReactiveAdapterRegistry reactiveAdapterRegistry;

	@Alias
	private boolean ignoreDefaultModelOnRedirect;

	@Alias
	private int cacheSecondsForSessionAttributeHandlers;

	@Alias
	private boolean synchronizeOnSession;

	@Alias
	private SessionAttributeStore sessionAttributeStore;

	@Alias
	private ParameterNameDiscoverer parameterNameDiscoverer;

	@Alias
	private Map<Class<?>, SessionAttributesHandler> sessionAttributesHandlerCache;

	@Alias
	private Map<Class<?>, Set<Method>> initBinderCache;

	@Alias
	private Map<ControllerAdviceBean, Set<Method>> initBinderAdviceCache;

	@Alias
	private Map<Class<?>, Set<Method>> modelAttributeCache;

	@Alias
	private Map<ControllerAdviceBean, Set<Method>> modelAttributeAdviceCache;

	@Substitute
	public Target_RequestMappingHandlerAdapter() {
		this.requestResponseBodyAdvice =  new ArrayList<>();
		this.messageConverters = new ArrayList<>(2);
		this.messageConverters.add(new ByteArrayHttpMessageConverter());
		this.messageConverters.add(new StringHttpMessageConverter());
		this.messageConverters.add(new FormHttpMessageConverter()); // Impossible to create a substitution for AllEncompassingFormHttpMessageConverter for now so we use that one
		this.logger = LogFactory.getLog(getClass());
		this.contentNegotiationManager = new ContentNegotiationManager();
		this.taskExecutor = new SimpleAsyncTaskExecutor("MvcAsync");
		this.callableInterceptors = new CallableProcessingInterceptor[0];
		this.deferredResultInterceptors = new DeferredResultProcessingInterceptor[0];
		this.reactiveAdapterRegistry = ReactiveAdapterRegistry.getSharedInstance();
		this.sessionAttributeStore = new DefaultSessionAttributeStore();
		this.parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
		this.sessionAttributesHandlerCache = new ConcurrentHashMap<>(64);
		this.ignoreDefaultModelOnRedirect = false;
		this.cacheSecondsForSessionAttributeHandlers = 0;
		this.synchronizeOnSession = false;
		this.initBinderCache = new ConcurrentHashMap<>(64);
		this.initBinderAdviceCache = new LinkedHashMap<>();
		this.modelAttributeCache = new ConcurrentHashMap<>(64);
		this.modelAttributeAdviceCache = new LinkedHashMap<>();
	}

}
