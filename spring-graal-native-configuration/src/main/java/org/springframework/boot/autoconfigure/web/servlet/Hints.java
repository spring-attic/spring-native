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

import org.apache.catalina.servlets.DefaultServlet;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration.BeanPostProcessorsRegistrar;
import org.springframework.boot.web.server.WebServerFactoryCustomizerBeanPostProcessor;
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
		}),
		@TypeInfo(types= {DefaultServlet.class},access=AccessBits.CLASS|AccessBits.PUBLIC_CONSTRUCTORS|AccessBits.PUBLIC_METHODS)
})
/*
proposedHints.put("Lorg/springframework/boot/autoconfigure/web/servlet/WebMvcAutoConfiguration;",
		new CompilationHint(true, false, new String[] {
			"java.util.concurrent.Callable:EXISTENCE_MC"
		}));
*/
@ConfigurationHint(value=WebMvcAutoConfiguration.class, typeInfos = {
		@TypeInfo(typeNames= {"org.springframework.web.servlet.handler.AbstractHandlerMethodMapping$EmptyHandler"}),
		@TypeInfo(types= {Callable.class},access=AccessBits.CLASS|AccessBits.PUBLIC_METHODS|AccessBits.PUBLIC_CONSTRUCTORS)},abortIfTypesMissing = true)
// TODO this is an interesting one as it is hinted at by both flavours of BeanPostProcessorsRegistrar (reactive and servlet)
@ConfigurationHint(value=BeanPostProcessorsRegistrar.class,typeInfos= {
		@TypeInfo(types= {WebServerFactoryCustomizerBeanPostProcessor.class},access=AccessBits.CLASS|AccessBits.PUBLIC_CONSTRUCTORS)
})
public class Hints implements NativeImageConfiguration {
}
