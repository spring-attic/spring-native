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
package org.springframework.boot.autoconfigure.web.reactive;

import reactor.netty.DisposableServer;

import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration.BeanPostProcessorsRegistrar;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryConfiguration.EmbeddedTomcat;
import org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebServerApplicationContext;
import org.springframework.boot.web.server.WebServerFactoryCustomizerBeanPostProcessor;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.ResourcesInfo;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;
import org.springframework.web.reactive.HandlerResult;

@NativeImageHint(trigger=WebFluxAutoConfiguration.class,typeInfos = {
	@TypeInfo(types= { HandlerResult.class}, access=AccessBits.CLASS|AccessBits.DECLARED_CONSTRUCTORS),
	@TypeInfo(typeNames = "org.springframework.web.reactive.result.method.AbstractHandlerMethodMapping$PreFlightAmbiguousMatchHandler",
	access=AccessBits.CLASS|AccessBits.DECLARED_CONSTRUCTORS|AccessBits.DECLARED_METHODS)
})
@NativeImageHint(trigger=BeanPostProcessorsRegistrar.class,typeInfos= {
		@TypeInfo(types= {WebServerFactoryCustomizerBeanPostProcessor.class},access=AccessBits.CLASS|AccessBits.DECLARED_CONSTRUCTORS)
})
@NativeImageHint(trigger=ReactiveWebServerFactoryAutoConfiguration.class, typeInfos = { 
		@TypeInfo(
				types= {AnnotationConfigReactiveWebServerApplicationContext.class,DisposableServer.class
				},access=AccessBits.CLASS|AccessBits.DECLARED_CONSTRUCTORS|AccessBits.DECLARED_METHODS)
})
@NativeImageHint(trigger=EmbeddedTomcat.class,resourcesInfos = {
		// Accessed from org.apache.catalina.startup.Tomcat
		@ResourcesInfo(patterns="org/apache/catalina/startup/MimeTypeMappings.properties")
})
public class WebFluxHints implements NativeImageConfiguration {
}
