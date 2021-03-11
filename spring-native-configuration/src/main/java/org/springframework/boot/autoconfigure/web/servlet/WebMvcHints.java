/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.web.servlet;

import java.util.concurrent.Callable;

import org.apache.tomcat.util.descriptor.web.ErrorPage;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration.BeanPostProcessorsRegistrar;
import org.springframework.boot.autoconfigure.web.servlet.error.DefaultErrorViewResolver;
import org.springframework.boot.web.server.ErrorPageRegistrarBeanPostProcessor;
import org.springframework.boot.web.server.WebServerFactoryCustomizerBeanPostProcessor;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.InternalResourceView;


@NativeHint(trigger = WebMvcAutoConfiguration.class,
	resources = @ResourceHint(patterns="org/springframework/web/util/HtmlCharacterEntityReferences.properties"),
	types = {
			@TypeHint(types = {
				AnnotationConfigServletWebServerApplicationContext.class,
				ConfigurableWebApplicationContext.class,
				WebApplicationContext.class,
			}),
		@TypeHint(types= {
				ErrorPage.class,
				DefaultErrorViewResolver.class,
				BeanNameViewResolver.class,
				ErrorPageRegistrarBeanPostProcessor.class}
		),
		@TypeHint(
				typeNames = "org.springframework.web.servlet.handler.AbstractHandlerMethodMapping$EmptyHandler",
				access = AccessBits.LOAD_AND_CONSTRUCT | AccessBits.DECLARED_METHODS),
		@TypeHint(
				types = Callable.class,
				access = AccessBits.LOAD_AND_CONSTRUCT | AccessBits.DECLARED_METHODS),
		@TypeHint(types = InternalResourceView.class)
}, abortIfTypesMissing = true)
// TODO this is an interesting one as it is hinted at by both flavours of BeanPostProcessorsRegistrar (reactive and servlet)
@NativeHint(trigger = BeanPostProcessorsRegistrar.class, types = @TypeHint(types= WebServerFactoryCustomizerBeanPostProcessor.class))
public class WebMvcHints implements NativeConfiguration {
}
