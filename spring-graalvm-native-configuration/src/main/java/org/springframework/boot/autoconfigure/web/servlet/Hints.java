/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.boot.autoconfigure.web.servlet;

import java.util.concurrent.Callable;

import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.AbstractHttp11JsseProtocol;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.descriptor.web.ErrorPage;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration.BeanPostProcessorsRegistrar;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.DefaultErrorViewResolver;
import org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedWebappClassLoader;
import org.springframework.boot.web.server.AbstractConfigurableWebServerFactory;
import org.springframework.boot.web.server.ErrorPageRegistrarBeanPostProcessor;
import org.springframework.boot.web.server.WebServerFactoryCustomizerBeanPostProcessor;
import org.springframework.boot.web.servlet.AbstractFilterRegistrationBean;
import org.springframework.boot.web.servlet.DynamicRegistrationBean;
import org.springframework.boot.web.servlet.RegistrationBean;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.boot.web.servlet.filter.OrderedCharacterEncodingFilter;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.boot.web.servlet.filter.OrderedRequestContextFilter;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;
import org.springframework.http.MediaTypeFactory;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.FrameworkServlet;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.HttpServletBean;
import org.springframework.web.servlet.RequestToViewNameTranslator;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.function.support.HandlerFunctionAdapter;
import org.springframework.web.servlet.function.support.RouterFunctionMapping;
import org.springframework.web.servlet.handler.AbstractDetectingUrlHandlerMapping;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;
import org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping;
import org.springframework.web.servlet.handler.HandlerExceptionResolverComposite;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import org.springframework.web.servlet.handler.MatchableHandlerMapping;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter;
import org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter;
import org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver;
import org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;
import org.springframework.web.servlet.resource.ResourceUrlProvider;
import org.springframework.web.servlet.support.AbstractFlashMapManager;
import org.springframework.web.servlet.support.SessionFlashMapManager;
import org.springframework.web.servlet.support.WebContentGenerator;
import org.springframework.web.servlet.theme.AbstractThemeResolver;
import org.springframework.web.servlet.theme.FixedThemeResolver;
import org.springframework.web.servlet.view.AbstractCachingViewResolver;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.DefaultRequestToViewNameTranslator;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.servlet.view.ViewResolverComposite;
import org.springframework.web.util.HtmlUtils;


// These types come from DispatcherServlet.properties - maybe this hint should point to a properties file containing class names?
@NativeImageHint(trigger=DispatcherServletAutoConfiguration.class,typeInfos = {
	@TypeInfo(types= {
		HtmlUtils.class,MediaTypeFactory.class,
		AcceptHeaderLocaleResolver.class,FixedThemeResolver.class,BeanNameUrlHandlerMapping.class,RequestMappingHandlerMapping.class,RouterFunctionMapping.class,
		HttpRequestHandlerAdapter.class,SimpleControllerHandlerAdapter.class,RequestMappingHandlerAdapter.class,
		HandlerFunctionAdapter.class,ExceptionHandlerExceptionResolver.class,ResponseStatusExceptionResolver.class,
		DefaultHandlerExceptionResolver.class,DefaultRequestToViewNameTranslator.class,InternalResourceViewResolver.class,
		InternalResourceView.class, SessionFlashMapManager.class,
		// vvv these are pulled in due to actuator testing but probably necessary for general app support
		MatchableHandlerMapping.class,SimpleUrlHandlerMapping.class,HandlerMappingIntrospector.class,
		AbstractDetectingUrlHandlerMapping.class,AbstractHandlerMapping.class,AbstractHandlerMethodMapping.class,
		AbstractUrlHandlerMapping.class,HandlerExceptionResolverComposite.class,AbstractHandlerMethodAdapter.class,
		ResourceUrlProvider.class,AbstractFlashMapManager.class,WebContentGenerator.class,
		AbstractThemeResolver.class,AbstractCachingViewResolver.class,UrlBasedViewResolver.class,ViewResolverComposite.class,
		ViewResolver.class,FrameworkServlet.class,HandlerAdapter.class,HandlerExceptionResolver.class,HandlerMapping.class,
		FlashMapManager.class,HttpServletBean.class,RequestToViewNameTranslator.class,ThemeResolver.class,
		RegistrationBean.class,DynamicRegistrationBean.class,ServletContextInitializer.class,ServletRegistrationBean.class,
		OrderedCharacterEncodingFilter.class,OrderedFilter.class,OrderedRequestContextFilter.class,
		AbstractConfigurableWebServerFactory.class,
		DispatcherServletPath.class,AbstractErrorController.class,
		AbstractServletWebServerFactory.class,AbstractFilterRegistrationBean.class
		})
})
@NativeImageHint(trigger=WebMvcAutoConfiguration.class, typeInfos = {
		@TypeInfo(types= {AnnotationConfigServletWebServerApplicationContext.class,
				DefaultErrorViewResolver.class,
				// TODO Maybe the first and last of these 3 needs to be in a more generic configuration hint working for both reactive and servlet
				ConfigurableWebApplicationContext.class,TomcatEmbeddedWebappClassLoader.class,WebApplicationContext.class,
	ProtocolHandler.class,AbstractProtocol.class,AbstractHttp11Protocol.class,AbstractHttp11JsseProtocol.class,Http11NioProtocol.class,
	ErrorPage.class,DefaultErrorViewResolver.class,BeanNameViewResolver.class,
				ErrorPageRegistrarBeanPostProcessor.class},
				typeNames= {"org.springframework.web.servlet.handler.AbstractHandlerMethodMapping$EmptyHandler"}),
		@TypeInfo(types= {Callable.class},access=AccessBits.CLASS|AccessBits.DECLARED_METHODS|AccessBits.DECLARED_CONSTRUCTORS)},abortIfTypesMissing = true)
// TODO this is an interesting one as it is hinted at by both flavours of BeanPostProcessorsRegistrar (reactive and servlet)
@NativeImageHint(trigger=BeanPostProcessorsRegistrar.class,typeInfos= {
		@TypeInfo(types= {WebServerFactoryCustomizerBeanPostProcessor.class},access=AccessBits.CLASS|AccessBits.DECLARED_CONSTRUCTORS)
})
public class Hints implements NativeImageConfiguration {
}
