package org.springframework.web.servlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.apache.commons.logging.Log;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.graalvm.substitutions.OnlyPresent;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.function.support.HandlerFunctionAdapter;
import org.springframework.web.servlet.function.support.RouterFunctionMapping;
import org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter;
import org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter;
import org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;
import org.springframework.web.servlet.support.SessionFlashMapManager;
import org.springframework.web.servlet.theme.FixedThemeResolver;
import org.springframework.web.servlet.view.DefaultRequestToViewNameTranslator;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@TargetClass(className = "org.springframework.web.servlet.DispatcherServlet", onlyWith = { OnlyPresent.class })
final class Target_DispatcherServlet {

	@Alias
	protected Log logger;

	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.Reset, isFinal = true)
	private static Properties defaultStrategies;

	@Alias
	private MultipartResolver multipartResolver;

	@Alias
	private LocaleResolver localeResolver;

	@Alias
	private ThemeResolver themeResolver;

	@Alias
	private List<HandlerMapping> handlerMappings;

	@Alias
	private List<HandlerAdapter> handlerAdapters;

	@Alias
	private List<HandlerExceptionResolver> handlerExceptionResolvers;

	@Alias
	private RequestToViewNameTranslator viewNameTranslator;

	@Alias
	private FlashMapManager flashMapManager;

	@Alias
	private List<ViewResolver> viewResolvers;

	@Alias
	private boolean detectAllHandlerMappings;

	@Alias
	private boolean detectAllHandlerAdapters;

	@Alias
	private boolean detectAllHandlerExceptionResolvers;

	@Alias
	private boolean detectAllViewResolvers;

	@Alias
	private boolean throwExceptionIfNoHandlerFound;

	@Alias
	private boolean cleanupAfterInclude;


	@Substitute
	private void initMultipartResolver(ApplicationContext context) {
		try {
			this.multipartResolver = context.getBean("multipartResolver", MultipartResolver.class);
			if (logger.isTraceEnabled()) {
				logger.trace("Detected " + this.multipartResolver);
			}
			else if (logger.isDebugEnabled()) {
				logger.debug("Detected " + this.multipartResolver.getClass().getSimpleName());
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// Default is no multipart resolver.
			this.multipartResolver = null;
			if (logger.isTraceEnabled()) {
				logger.trace("No MultipartResolver multipartResolver declared");
			}
		}
	}

	@Substitute
	private void initLocaleResolver(ApplicationContext context) {
		try {
			this.localeResolver = context.getBean("localeResolver", LocaleResolver.class);
			if (logger.isTraceEnabled()) {
				logger.trace("Detected " + this.localeResolver);
			}
			else if (logger.isDebugEnabled()) {
				logger.debug("Detected " + this.localeResolver.getClass().getSimpleName());
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// We need to use the default.
			((GenericApplicationContext)context).registerBean("localeResolver", LocaleResolver.class, AcceptHeaderLocaleResolver::new);
			this.localeResolver = context.getBean("localeResolver", LocaleResolver.class);
			if (logger.isTraceEnabled()) {
				logger.trace("No LocaleResolver 'localeResolver': using default [" + this.localeResolver.getClass().getSimpleName() + "]");
			}
		}
	}

	@Substitute
	private void initThemeResolver(ApplicationContext context) {
		try {
			this.themeResolver = context.getBean("themeResolver", ThemeResolver.class);
			if (logger.isTraceEnabled()) {
				logger.trace("Detected " + this.themeResolver);
			}
			else if (logger.isDebugEnabled()) {
				logger.debug("Detected " + this.themeResolver.getClass().getSimpleName());
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// We need to use the default.
			((GenericApplicationContext)context).registerBean("themeResolver", ThemeResolver.class, FixedThemeResolver::new);
			this.themeResolver = context.getBean("themeResolver", ThemeResolver.class);
			if (logger.isTraceEnabled()) {
				logger.trace("No ThemeResolver 'themeResolver': using default [" + this.themeResolver.getClass().getSimpleName() + "]");
			}
		}
	}

	@Substitute
	private void initHandlerMappings(ApplicationContext context) {
		this.handlerMappings = null;

		if (this.detectAllHandlerMappings) {
			// Find all HandlerMappings in the ApplicationContext, including ancestor contexts.
			Map<String, HandlerMapping> matchingBeans =
					BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.handlerMappings = new ArrayList<>(matchingBeans.values());
				// We keep HandlerMappings in sorted order.
				AnnotationAwareOrderComparator.sort(this.handlerMappings);
			}
		}
		else {
			try {
				HandlerMapping hm = context.getBean("handlerMapping", HandlerMapping.class);
				this.handlerMappings = Collections.singletonList(hm);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore, we'll add a default HandlerMapping later.
			}
		}

		// Ensure we have at least one HandlerMapping, by registering
		// a default HandlerMapping if no other mappings are found.
		if (this.handlerMappings == null) {
			((GenericApplicationContext)context).registerBean(HandlerMapping.class, BeanNameUrlHandlerMapping::new);
			((GenericApplicationContext)context).registerBean(HandlerMapping.class, RequestMappingHandlerMapping::new);
			((GenericApplicationContext)context).registerBean(HandlerMapping.class, (Supplier<HandlerMapping>) RouterFunctionMapping::new);
			this.handlerMappings = (List<HandlerMapping>) context.getBeansOfType(HandlerMapping.class).values();
			if (logger.isTraceEnabled()) {
				logger.trace("No HandlerMappings declared, using default strategies from DispatcherServlet.properties");
			}
		}
	}

	@Substitute
	private void initHandlerAdapters(ApplicationContext context) {
		this.handlerAdapters = null;

		if (this.detectAllHandlerAdapters) {
			// Find all HandlerAdapters in the ApplicationContext, including ancestor contexts.
			Map<String, HandlerAdapter> matchingBeans =
					BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerAdapter.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.handlerAdapters = new ArrayList<>(matchingBeans.values());
				// We keep HandlerAdapters in sorted order.
				AnnotationAwareOrderComparator.sort(this.handlerAdapters);
			}
		}
		else {
			try {
				HandlerAdapter ha = context.getBean("handlerAdapter", HandlerAdapter.class);
				this.handlerAdapters = Collections.singletonList(ha);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore, we'll add a default HandlerAdapter later.
			}
		}

		// Ensure we have at least some HandlerAdapters, by registering
		// default HandlerAdapters if no other adapters are found.
		if (this.handlerAdapters == null) {
			((GenericApplicationContext)context).registerBean(HandlerAdapter.class, HttpRequestHandlerAdapter::new);
			((GenericApplicationContext)context).registerBean(HandlerAdapter.class, SimpleControllerHandlerAdapter::new);
			((GenericApplicationContext)context).registerBean(HandlerAdapter.class, RequestMappingHandlerAdapter::new);
			((GenericApplicationContext)context).registerBean(HandlerAdapter.class, HandlerFunctionAdapter::new);
			this.handlerAdapters = (List<HandlerAdapter>) context.getBeansOfType(HandlerAdapter.class).values();
			if (logger.isTraceEnabled()) {
				logger.trace("No HandlerAdapters declared using default strategies from DispatcherServlet.properties");
			}
		}
	}

	@Substitute
	private void initHandlerExceptionResolvers(ApplicationContext context) {
		this.handlerExceptionResolvers = null;

		if (this.detectAllHandlerExceptionResolvers) {
			// Find all HandlerExceptionResolvers in the ApplicationContext, including ancestor contexts.
			Map<String, HandlerExceptionResolver> matchingBeans = BeanFactoryUtils
					.beansOfTypeIncludingAncestors(context, HandlerExceptionResolver.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.handlerExceptionResolvers = new ArrayList<>(matchingBeans.values());
				// We keep HandlerExceptionResolvers in sorted order.
				AnnotationAwareOrderComparator.sort(this.handlerExceptionResolvers);
			}
		}
		else {
			try {
				HandlerExceptionResolver her =
						context.getBean("handlerExceptionResolver", HandlerExceptionResolver.class);
				this.handlerExceptionResolvers = Collections.singletonList(her);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore, no HandlerExceptionResolver is fine too.
			}
		}

		// Ensure we have at least some HandlerExceptionResolvers, by registering
		// default HandlerExceptionResolvers if no other resolvers are found.
		if (this.handlerExceptionResolvers == null) {
			((GenericApplicationContext)context).registerBean(HandlerExceptionResolver.class, ExceptionHandlerExceptionResolver::new);
			((GenericApplicationContext)context).registerBean(HandlerExceptionResolver.class, ResponseStatusExceptionResolver::new);
			((GenericApplicationContext)context).registerBean(HandlerExceptionResolver.class, DefaultHandlerExceptionResolver::new);
			this.handlerExceptionResolvers = (List<HandlerExceptionResolver>) context.getBeansOfType(HandlerExceptionResolver.class).values();
			if (logger.isTraceEnabled()) {
				logger.trace("No HandlerExceptionResolvers declared using default strategies from DispatcherServlet.properties");
			}
		}
	}

	@Substitute
	private void initRequestToViewNameTranslator(ApplicationContext context) {
		try {
			this.viewNameTranslator =
					context.getBean("viewNameTranslator", RequestToViewNameTranslator.class);
			if (logger.isTraceEnabled()) {
				logger.trace("Detected " + this.viewNameTranslator.getClass().getSimpleName());
			}
			else if (logger.isDebugEnabled()) {
				logger.debug("Detected " + this.viewNameTranslator);
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// We need to use the default.
			((GenericApplicationContext)context).registerBean("viewNameTranslator", RequestToViewNameTranslator.class, DefaultRequestToViewNameTranslator::new);
			this.viewNameTranslator = context.getBean("viewNameTranslator", RequestToViewNameTranslator.class);
			if (logger.isTraceEnabled()) {
				logger.trace("No RequestToViewNameTranslator 'viewNameTranslator': using default [" + this.viewNameTranslator.getClass().getSimpleName() + "]");
			}
		}
	}

	@Substitute
	private void initViewResolvers(ApplicationContext context) {
		this.viewResolvers = null;

		if (this.detectAllViewResolvers) {
			// Find all ViewResolvers in the ApplicationContext, including ancestor contexts.
			Map<String, ViewResolver> matchingBeans =
					BeanFactoryUtils.beansOfTypeIncludingAncestors(context, ViewResolver.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.viewResolvers = new ArrayList<>(matchingBeans.values());
				// We keep ViewResolvers in sorted order.
				AnnotationAwareOrderComparator.sort(this.viewResolvers);
			}
		}
		else {
			try {
				ViewResolver vr = context.getBean("viewResolver", ViewResolver.class);
				this.viewResolvers = Collections.singletonList(vr);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore, we'll add a default ViewResolver later.
			}
		}

		// Ensure we have at least one ViewResolver, by registering
		// a default ViewResolver if no other resolvers are found.
		if (this.viewResolvers == null) {
			((GenericApplicationContext)context).registerBean("viewResolver", ViewResolver.class, InternalResourceViewResolver::new);
			this.viewResolvers = (List<ViewResolver>) context.getBeansOfType(ViewResolver.class).values();
			if (logger.isTraceEnabled()) {
				logger.trace("No ViewResolvers declared using default strategies from DispatcherServlet.properties");
			}
		}
	}

	@Substitute
	private void initFlashMapManager(ApplicationContext context) {
		try {
			this.flashMapManager = context.getBean("flashMapManager", FlashMapManager.class);
			if (logger.isTraceEnabled()) {
				logger.trace("Detected " + this.flashMapManager.getClass().getSimpleName());
			}
			else if (logger.isDebugEnabled()) {
				logger.debug("Detected " + this.flashMapManager);
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// We need to use the default.
			((GenericApplicationContext)context).registerBean("flashMapManager", FlashMapManager.class, SessionFlashMapManager::new);
			this.flashMapManager = context.getBean("flashMapManager", FlashMapManager.class);
			if (logger.isTraceEnabled()) {
				logger.trace("No FlashMapManager 'flashMapManager': using default [" + this.flashMapManager.getClass().getSimpleName() + "]");
			}
		}
	}

}
