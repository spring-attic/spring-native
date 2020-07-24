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
package org.springframework.boot.actuate.autoconfigure.endpoint.web.servlet;

import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.ServletEndpointManagementContextConfiguration.WebMvcServletEndpointManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.reactive.WebFluxEndpointManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextFactory;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextType;
import org.springframework.boot.actuate.autoconfigure.web.reactive.ReactiveManagementChildContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.actuate.endpoint.web.PathMapper;
import org.springframework.boot.actuate.endpoint.web.ServletEndpointRegistrar;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointDiscoverer;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpointDiscoverer;
import org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.boot.actuate.logging.LogFileWebEndpoint;
import org.springframework.boot.actuate.management.HeapDumpWebEndpoint;
import org.springframework.boot.actuate.trace.http.HttpTraceEndpoint;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.TypeSystem;

// The configurations related to actuator are in this key in spring.factories:
// org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration
// NOT EnableAutoConfiguration - this means Spring GraalVM Native is not fully processing them
// For now - rather than activating type chasing processing for that key as well, let's
// hook these hints on AutoConfigurations.
// So the management configuration is WebMvcEndpointManagementContextConfiguration
@NativeImageHint(trigger=WebEndpointAutoConfiguration.class, 
	importInfos = { CommonWebActuatorTypes.class},
 	typeInfos = {
 		@TypeInfo(types = {
		HttpTraceEndpoint.class,
		LogFileWebEndpoint.class,
		WebMvcEndpointHandlerMapping.class,
		ServletEndpointRegistrar.class,
		AbstractWebMvcEndpointHandlerMapping.class,	
		WebMvcServletEndpointManagementContextConfiguration.class,
		ControllerEndpointDiscoverer.class,
		ControllerEndpointsSupplier.class,
		org.springframework.boot.actuate.autoconfigure.endpoint.web.ServletEndpointManagementContextConfiguration.class,
		org.springframework.boot.actuate.autoconfigure.web.jersey.JerseySameManagementContextConfiguration.class,
		org.springframework.boot.actuate.autoconfigure.web.jersey.JerseyChildManagementContextConfiguration.class,
		HttpTraceRepository.class,
		org.springframework.boot.autoconfigure.web.ErrorProperties.class,
		org.springframework.boot.autoconfigure.web.ErrorProperties.IncludeAttribute.class,
		org.springframework.boot.autoconfigure.web.ErrorProperties.IncludeStacktrace.class,
		org.springframework.boot.autoconfigure.web.ErrorProperties.Whitelabel.class,
		org.springframework.boot.autoconfigure.web.ResourceProperties.Cache.class,
		org.springframework.boot.autoconfigure.web.ResourceProperties.Cache.Cachecontrol.class,
		org.springframework.boot.autoconfigure.web.ResourceProperties.Chain.class,
		org.springframework.boot.autoconfigure.web.ResourceProperties.Content.class,
		org.springframework.boot.autoconfigure.web.ResourceProperties.Fixed.class,
		org.springframework.boot.autoconfigure.web.ResourceProperties.Strategy.class,
		org.springframework.boot.autoconfigure.web.ServerProperties.ForwardHeadersStrategy.class,
		org.springframework.boot.autoconfigure.web.ServerProperties.Jetty.class,
		org.springframework.boot.autoconfigure.web.ServerProperties.Jetty.Accesslog.class,
		org.springframework.boot.autoconfigure.web.ServerProperties.Jetty.Accesslog.FORMAT.class,
		org.springframework.boot.autoconfigure.web.ServerProperties.Jetty.Threads.class,
		org.springframework.boot.autoconfigure.web.ServerProperties.Netty.class,
		org.springframework.boot.autoconfigure.web.ServerProperties.Tomcat.class,
		org.springframework.boot.autoconfigure.web.ServerProperties.Tomcat.Accesslog.class,
		org.springframework.boot.autoconfigure.web.ServerProperties.Tomcat.Mbeanregistry.class,
		org.springframework.boot.autoconfigure.web.ServerProperties.Tomcat.Remoteip.class,
		org.springframework.boot.autoconfigure.web.ServerProperties.Tomcat.Resource.class,
		org.springframework.boot.autoconfigure.web.ServerProperties.Tomcat.Threads.class,
		org.springframework.boot.autoconfigure.web.ServerProperties.Undertow.class,
		org.springframework.boot.autoconfigure.web.ServerProperties.Undertow.Accesslog.class,
		org.springframework.boot.autoconfigure.web.ServerProperties.Undertow.Options.class,
		org.springframework.boot.autoconfigure.web.ServerProperties.Undertow.Threads.class,
		WebFluxEndpointManagementContextConfiguration.class,
		ManagementContextType.class,
		HeapDumpWebEndpoint.class,
		EndpointMediaTypes.class,
		PathMappedEndpoints.class,
		WebEndpointsSupplier.class,
		CorsEndpointProperties.class,
		EndpointWebExtension.class,
		WebEndpoint.class,
		WebEndpointDiscoverer.class,
		// web package
		ManagementContextConfiguration.class,
		ManagementContextFactory.class,
		ManagementContextType.class,
		PathMapper.class,
		ManagementPortType.class,
		org.springframework.boot.actuate.endpoint.web.servlet.ControllerEndpointHandlerMapping.class,
		org.springframework.boot.actuate.autoconfigure.endpoint.web.servlet.WebMvcEndpointManagementContextConfiguration.class,
		org.springframework.boot.actuate.autoconfigure.security.servlet.SecurityRequestMatchersManagementContextConfiguration.class,
		// This reactive one is required even though not a webflux app, awesome...
		ReactiveManagementChildContextConfiguration.class,
	}, typeNames = {
		"org.springframework.boot.actuate.autoconfigure.web.server.EnableManagementContext",
		"org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration$SameManagementContextConfiguration",
		"org.springframework.boot.actuate.autoconfigure.endpoint.web.jersey.JerseyWebEndpointManagementContextConfiguration",
		"org.springframework.boot.actuate.autoconfigure.endpoint.web.MappingWebEndpointPathMapper",
		"org.springframework.boot.actuate.endpoint.web.annotation.WebEndpointFilter",
		"org.springframework.boot.actuate.autoconfigure.web.servlet.ServletManagementChildContextConfiguration",
		"org.springframework.boot.actuate.autoconfigure.web.servlet.WebMvcEndpointChildContextConfiguration",
		"org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping$LinksHandler",
		"org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping$OperationHandler",
		"org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping$WebMvcLinksHandler",
	})
})
public class WebMvcEndpointManagementContextConfigurationHints implements NativeImageConfiguration {

	@Override
	public boolean isValid(TypeSystem typeSystem) {
		// Similar to check in OnWebApplicationCondition (effectively implementing ConditionalOnWebApplication(SERVLET))
		boolean isWebMvcApplication = typeSystem.resolveName("org.springframework.web.context.support.GenericWebApplicationContext", true) != null;
		return isWebMvcApplication;
	}

}