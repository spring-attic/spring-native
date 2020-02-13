package org.springframework.boot.autoconfigure.web.servlet;

import java.util.concurrent.Callable;

import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.graal.extension.TypeInfo;
import org.springframework.graal.type.AccessBits;
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


// These types come from DispatcherServlet.properties - maybe this hint should point to a properties file containing class names?
@ConfigurationHint(value=DispatcherServletAutoConfiguration.class,typeInfos = {
	@TypeInfo(types= {AcceptHeaderLocaleResolver.class,FixedThemeResolver.class,BeanNameUrlHandlerMapping.class,RequestMappingHandlerMapping.class,RouterFunctionMapping.class,
		HttpRequestHandlerAdapter.class,SimpleControllerHandlerAdapter.class,RequestMappingHandlerAdapter.class,
		HandlerFunctionAdapter.class,ExceptionHandlerExceptionResolver.class,ResponseStatusExceptionResolver.class,
		DefaultHandlerExceptionResolver.class,DefaultRequestToViewNameTranslator.class,InternalResourceViewResolver.class,SessionFlashMapManager.class
		})	
})
/*
proposedHints.put("Lorg/springframework/boot/autoconfigure/web/servlet/WebMvcAutoConfiguration;",
		new CompilationHint(true, false, new String[] {
			"java.util.concurrent.Callable:EXISTENCE_MC"
		}));
*/
@ConfigurationHint(value=WebMvcAutoConfiguration.class, typeInfos = {@TypeInfo(types= {Callable.class},access=AccessBits.CLASS|AccessBits.PUBLIC_METHODS|AccessBits.PUBLIC_CONSTRUCTORS)},abortIfTypesMissing = true)
public class Hints implements NativeImageConfiguration {
}
