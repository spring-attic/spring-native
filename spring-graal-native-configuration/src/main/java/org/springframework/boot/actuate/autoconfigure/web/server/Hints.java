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
package org.springframework.boot.actuate.autoconfigure.web.server;

import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextType;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.EndpointExtension;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.Selector.Match;
import org.springframework.boot.actuate.endpoint.http.ApiVersion;
import org.springframework.boot.actuate.logging.LogFileWebEndpoint;
import org.springframework.boot.autoconfigure.web.embedded.JettyWebServerFactoryCustomizer;
import org.springframework.boot.autoconfigure.web.embedded.NettyWebServerFactoryCustomizer;
import org.springframework.boot.autoconfigure.web.embedded.TomcatWebServerFactoryCustomizer;
import org.springframework.boot.autoconfigure.web.embedded.UndertowWebServerFactoryCustomizer;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryCustomizer;
import org.springframework.boot.autoconfigure.web.reactive.TomcatReactiveWebServerFactoryCustomizer;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.graal.extension.NativeImageHint;
import org.springframework.graal.extension.TypeInfo;
import org.springframework.boot.actuate.endpoint.web.Link;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.boot.actuate.endpoint.web.reactive.ControllerEndpointHandlerMapping;
import org.springframework.boot.actuate.endpoint.web.reactive.WebFluxEndpointHandlerMapping;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.HealthEndpoint;

@NativeImageHint(trigger = ManagementContextConfigurationImportSelector.class, typeInfos = { 
	@TypeInfo(types = {
		CloudPlatform.class, // TODO unsure exactly which configuration pulls this in - could optimize maybe
		Endpoint.class,
		EndpointExtension.class,
		WebEndpoint.class,
		ManagementPortType.class,
		ManagementContextType.class,

		LogFileWebEndpoint.class,
		CorsEndpointProperties.class,

		// TODO push out to hint on reactive actuator endpoint (need a webmvc actuator sample too!)
		WebFluxEndpointHandlerMapping.class,
		ControllerEndpointHandlerMapping.class,
		
//		 ReactiveWebServerFactoryCustomizer.class, TomcatWebServerFactoryCustomizer.class,
//			TomcatReactiveWebServerFactoryCustomizer.class, JettyWebServerFactoryCustomizer.class,
//			UndertowWebServerFactoryCustomizer.class, NettyWebServerFactoryCustomizer.class,
		Link.class, // serialization problem for this if hit /actuator
	HealthComponent.class,	
	HealthEndpoint.class,
	HealthContributor.class,
	ApiVersion.class,
	// these two for java.lang.IllegalStateException: Failed to extract parameter names for public org.springframework.boot.actuate.health.HealthComponent org.springframework.boot.actuate.health.HealthEndpoint.healthForPath(java.lang.String[])
	Selector.class,Match.class,
		EnableManagementContext.class,
		org.springframework.boot.actuate.autoconfigure.endpoint.web.ServletEndpointManagementContextConfiguration.class,
		org.springframework.boot.actuate.autoconfigure.endpoint.web.reactive.WebFluxEndpointManagementContextConfiguration.class,
		org.springframework.boot.actuate.autoconfigure.endpoint.web.servlet.WebMvcEndpointManagementContextConfiguration.class,
		org.springframework.boot.actuate.autoconfigure.security.servlet.SecurityRequestMatchersManagementContextConfiguration.class,
		org.springframework.boot.actuate.autoconfigure.web.jersey.JerseySameManagementContextConfiguration.class,
		org.springframework.boot.actuate.autoconfigure.web.jersey.JerseyChildManagementContextConfiguration.class,
		org.springframework.boot.actuate.autoconfigure.web.reactive.ReactiveManagementChildContextConfiguration.class 
	}, typeNames = {
			
		// TODO this next one, actuator looks like the first thing pushing on it but surely it isn't the only user and this should have a different trigger?
		"org.springframework.context.annotation.ConfigurationClassParser$DefaultDeferredImportSelectorGroup",

		"org.springframework.core.LocalVariableTableParameterNameDiscoverer",
		"org.springframework.boot.actuate.autoconfigure.endpoint.web.jersey.JerseyWebEndpointManagementContextConfiguration",
		"org.springframework.boot.actuate.autoconfigure.web.servlet.ServletManagementChildContextConfiguration",
		"org.springframework.boot.actuate.autoconfigure.web.servlet.WebMvcEndpointChildContextConfiguration" 
	})})
public class Hints implements NativeImageConfiguration {
}