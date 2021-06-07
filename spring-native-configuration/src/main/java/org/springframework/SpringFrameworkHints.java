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

package org.springframework;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.SynthesizedAnnotation;
import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.JdkProxyHint;

@NativeHint(
		initialization = @InitializationHint(types = {
				org.springframework.aop.TargetSource.class,
				org.springframework.aop.framework.Advised.class,
				org.springframework.aop.Advisor.class,
				org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.class,
				org.springframework.core.DecoratingProxy.class,
				org.springframework.core.annotation.AnnotationFilter.class,
				org.springframework.core.DefaultParameterNameDiscoverer.class,
				org.springframework.core.ResolvableType.class,
				org.springframework.core.io.support.SpringFactoriesLoader.class,
				org.springframework.jdbc.datasource.ConnectionProxy.class,
				org.springframework.jdbc.support.JdbcAccessor.class,
				org.springframework.jdbc.support.JdbcTransactionManager.class,
				org.springframework.http.HttpStatus.class,
				org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter.class,
				org.springframework.transaction.annotation.Isolation.class,
				org.springframework.transaction.annotation.Propagation.class,
				org.springframework.util.unit.DataUnit.class,
				org.springframework.util.unit.DataSize.class,
				org.springframework.util.Assert.class,
				org.springframework.util.StringUtils.class,
				// Caches
				org.springframework.beans.CachedIntrospectionResults.class,
				org.springframework.core.annotation.AnnotationUtils.class,
				org.springframework.util.ReflectionUtils.class,
				// For SPEL removal
				org.springframework.context.support.AbstractApplicationContext.class,
				org.springframework.context.event.EventListenerMethodProcessor.class,
				org.springframework.core.SpringProperties.class,
				// For XML removal
				org.springframework.core.io.support.PropertiesLoaderUtils.class,
				org.springframework.core.io.support.ResourcePropertiesPersister.class,
				org.springframework.messaging.simp.config.AbstractMessageBrokerConfiguration.class,
				org.springframework.http.MediaType.class,
				org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter.class,
				org.springframework.beans.factory.xml.XmlBeanDefinitionReader.class,
				org.springframework.beans.PropertyEditorRegistrySupport.class,
				org.springframework.util.ReflectionUtils.class,
				org.springframework.util.DefaultPropertiesPersister.class,
				org.springframework.util.ClassUtils.class,
				org.springframework.util.ConcurrentReferenceHashMap.class,
				org.springframework.util.CollectionUtils.class,
				org.springframework.util.LinkedCaseInsensitiveMap.class,
				org.springframework.util.MimeType.class,
				org.springframework.web.servlet.function.support.RouterFunctionMapping.class,
				org.springframework.web.client.RestTemplate.class,
				org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver.class,
				org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport.class,
				org.springframework.web.socket.sockjs.transport.TransportHandlingSockJsService.class,
				org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.class,
				org.springframework.core.ReactiveAdapterRegistry.class,
				org.springframework.core.NativeDetector.class,
				org.springframework.core.KotlinDetector.class,
				org.aopalliance.aop.Advice.class
		}, typeNames = {
				"org.springframework.aop.Advisor$1",
				"org.springframework.core.annotation.AnnotationFilter$1",
				"org.springframework.core.annotation.AnnotationFilter$2",
				"org.springframework.core.annotation.PackagesAnnotationFilter",
				"org.springframework.core.annotation.TypeMappedAnnotations",
				"org.springframework.format.annotation.DateTimeFormat$ISO",
				"org.springframework.http.codec.CodecConfigurerFactory",
				// For XML removal
				"org.springframework.http.codec.support.BaseDefaultCodecs"
		}, packageNames = {
				"org.springframework.core.env"
		}, initTime = InitializationTime.BUILD),
		jdkProxies = {
				// TODO this hint could be probably be inferred but let's review once functional config is there whether it is needed at all
				@JdkProxyHint(types= {
						Qualifier.class,
						SynthesizedAnnotation.class
				}),
				// TODO For a regular web app this one is auto added now. But the function-netty app doesn't include endpoints and yet
				// needs this proxy - is there infrastructure that includes a mapping that we need to analyse (for auto synthannotation proxy
				// generation?). Currently the analysis is only done for app components, not library infrastructure.
				@JdkProxyHint(types = {
						org.springframework.web.bind.annotation.RequestMapping.class,
						org.springframework.core.annotation.SynthesizedAnnotation.class
				}),
				@JdkProxyHint(types = {
						org.springframework.context.annotation.Lazy.class,
						org.springframework.core.annotation.SynthesizedAnnotation.class
				})

		}

)
public class SpringFrameworkHints implements NativeConfiguration {
}
