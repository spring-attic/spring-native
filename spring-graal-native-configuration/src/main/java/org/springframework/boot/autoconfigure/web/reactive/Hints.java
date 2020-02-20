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

import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration.BeanPostProcessorsRegistrar;
import org.springframework.boot.web.server.WebServerFactoryCustomizerBeanPostProcessor;
import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.graal.extension.TypeInfo;
import org.springframework.graal.type.AccessBits;
import org.springframework.http.codec.support.DefaultClientCodecConfigurer;
import org.springframework.http.codec.support.DefaultServerCodecConfigurer;
import org.springframework.util.ClassUtils;

@ConfigurationHint(value=WebFluxAutoConfiguration.class,typeInfos = {
	// These two believed through WebFluxConfigurationSupport, CodecConfigurer.properties
	@TypeInfo(types= {DefaultClientCodecConfigurer.class,DefaultServerCodecConfigurer.class},
			// These are from BaseDefaultCodecs - not sure on needed visibility
			// TODO Aren't these also needed for non reactive auto configuration web? Is there a common configuration supertype between those
			// configurations that they can be hung off
			typeNames= {
				"com.fasterxml.jackson.databind.ObjectMapper", 
				"com.fasterxml.jackson.core.JsonGenerator",
				"com.fasterxml.jackson.dataformat.smile.SmileFactory", 
				"javax.xml.bind.Binder", 
				"com.google.protobuf.Message", 
				"org.synchronoss.cloud.nio.multipart.NioMultipartParser"
			},
			access=AccessBits.CLASS|AccessBits.PUBLIC_CONSTRUCTORS
		)
})
@ConfigurationHint(value=BeanPostProcessorsRegistrar.class,typeInfos= {
		@TypeInfo(types= {WebServerFactoryCustomizerBeanPostProcessor.class},access=AccessBits.CLASS|AccessBits.PUBLIC_CONSTRUCTORS)
})
public class Hints implements NativeImageConfiguration {
}
