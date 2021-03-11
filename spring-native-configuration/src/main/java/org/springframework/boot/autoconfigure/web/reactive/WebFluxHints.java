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

package org.springframework.boot.autoconfigure.web.reactive;

import org.springframework.boot.autoconfigure.web.CommonWebInfos;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration.BeanPostProcessorsRegistrar;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryConfiguration.EmbeddedNetty;
import org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebServerApplicationContext;
import org.springframework.boot.web.server.WebServerFactoryCustomizerBeanPostProcessor;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.web.reactive.HandlerResult;

import reactor.core.publisher.Flux;
import reactor.netty.DisposableServer;

@NativeHint(trigger = WebFluxAutoConfiguration.class,
	resources = @ResourceHint(patterns = "org/springframework/web/util/HtmlCharacterEntityReferences.properties"),
	types = {
		@TypeHint(types= HandlerResult.class),
		@TypeHint(types = Flux.class, access = AccessBits.CLASS),
		@TypeHint(typeNames = "org.springframework.web.reactive.result.method.AbstractHandlerMethodMapping$PreFlightAmbiguousMatchHandler",
				access = AccessBits.CLASS | AccessBits.DECLARED_CONSTRUCTORS | AccessBits.DECLARED_METHODS)
})
@NativeHint(trigger = BeanPostProcessorsRegistrar.class, types = @TypeHint(types= WebServerFactoryCustomizerBeanPostProcessor.class))
@NativeHint(trigger = ReactiveWebServerFactoryAutoConfiguration.class, types = {
		@TypeHint(
				types= {
						AnnotationConfigReactiveWebServerApplicationContext.class,
						DisposableServer.class
				}, access = AccessBits.CLASS | AccessBits.DECLARED_CONSTRUCTORS | AccessBits.DECLARED_METHODS)
})
@NativeHint(trigger = EmbeddedNetty.class, imports = CommonWebInfos.class)
public class WebFluxHints implements NativeConfiguration {
}
