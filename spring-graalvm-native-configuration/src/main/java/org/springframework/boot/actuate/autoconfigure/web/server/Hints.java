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

import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.actuate.audit.AuditEventsEndpoint;
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.ExposeExcludePropertyEndpointFilter;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.ServletEndpointManagementContextConfiguration.WebMvcServletEndpointManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.reactive.WebFluxEndpointManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.health.HealthProperties;
import org.springframework.boot.actuate.autoconfigure.metrics.PropertiesMeterFilter;
import org.springframework.boot.actuate.autoconfigure.metrics.export.properties.PropertiesConfigAdapter;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimplePropertiesConfigAdapter;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextFactory;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextType;
import org.springframework.boot.actuate.autoconfigure.web.mappings.MappingsEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration.SameManagementContextConfiguration;
import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.boot.actuate.cache.CachesEndpoint;
import org.springframework.boot.actuate.context.ShutdownEndpoint;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.actuate.endpoint.EndpointFilter;
import org.springframework.boot.actuate.endpoint.EndpointsSupplier;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.EndpointConverter;
import org.springframework.boot.actuate.endpoint.annotation.EndpointDiscoverer;
import org.springframework.boot.actuate.endpoint.annotation.EndpointExtension;
import org.springframework.boot.actuate.endpoint.annotation.FilteredEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.Selector.Match;
import org.springframework.boot.actuate.endpoint.http.ApiVersion;
import org.springframework.boot.actuate.endpoint.invoke.OperationInvokerAdvisor;
import org.springframework.boot.actuate.endpoint.invoke.ParameterValueMapper;
import org.springframework.boot.actuate.endpoint.invoke.convert.ConversionServiceParameterValueMapper;
import org.springframework.boot.actuate.endpoint.invoker.cache.CachingOperationInvokerAdvisor;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.Link;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.actuate.endpoint.web.PathMapper;
import org.springframework.boot.actuate.endpoint.web.ServletEndpointRegistrar;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointDiscoverer;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpointDiscoverer;
import org.springframework.boot.actuate.endpoint.web.reactive.AbstractWebFluxEndpointHandlerMapping;
import org.springframework.boot.actuate.endpoint.web.reactive.ControllerEndpointHandlerMapping;
import org.springframework.boot.actuate.endpoint.web.reactive.WebFluxEndpointHandlerMapping;
import org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.boot.actuate.health.AbstractHealthAggregator;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.CompositeHealth;
import org.springframework.boot.actuate.health.ContributorRegistry;
import org.springframework.boot.actuate.health.DefaultHealthContributorRegistry;
import org.springframework.boot.actuate.health.DefaultReactiveHealthContributorRegistry;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.HealthContributorRegistry;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.HealthEndpointGroups;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicatorRegistry;
import org.springframework.boot.actuate.health.HttpCodeStatusMapper;
import org.springframework.boot.actuate.health.NamedContributors;
import org.springframework.boot.actuate.health.OrderedHealthAggregator;
import org.springframework.boot.actuate.health.PingHealthIndicator;
import org.springframework.boot.actuate.health.ReactiveHealthContributorRegistry;
import org.springframework.boot.actuate.health.ReactiveHealthEndpointWebExtension;
import org.springframework.boot.actuate.health.ReactiveHealthIndicatorRegistry;
import org.springframework.boot.actuate.health.SimpleHttpCodeStatusMapper;
import org.springframework.boot.actuate.health.SimpleStatusAggregator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.actuate.health.StatusAggregator;
import org.springframework.boot.actuate.health.SystemHealth;
import org.springframework.boot.actuate.info.EnvironmentInfoContributor;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.logging.LogFileWebEndpoint;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.boot.actuate.management.HeapDumpWebEndpoint;
import org.springframework.boot.actuate.management.ThreadDumpEndpoint;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.actuate.metrics.web.reactive.client.DefaultWebClientExchangeTagsProvider;
import org.springframework.boot.actuate.metrics.web.reactive.client.MetricsWebClientCustomizer;
import org.springframework.boot.actuate.metrics.web.reactive.client.WebClientExchangeTagsProvider;
import org.springframework.boot.actuate.metrics.web.reactive.server.DefaultWebFluxTagsProvider;
import org.springframework.boot.actuate.metrics.web.reactive.server.MetricsWebFilter;
import org.springframework.boot.actuate.metrics.web.reactive.server.WebFluxTagsProvider;
import org.springframework.boot.actuate.scheduling.ScheduledTasksEndpoint;
import org.springframework.boot.actuate.system.DiskSpaceHealthIndicator;
import org.springframework.boot.actuate.trace.http.HttpTraceEndpoint;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;

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

		org.springframework.boot.actuate.endpoint.web.servlet.ControllerEndpointHandlerMapping.class,
		WebMvcServletEndpointManagementContextConfiguration.class,
		
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
	HttpTraceRepository.class,
		EnableManagementContext.class,
		org.springframework.boot.actuate.autoconfigure.endpoint.web.ServletEndpointManagementContextConfiguration.class,
		org.springframework.boot.actuate.autoconfigure.endpoint.web.reactive.WebFluxEndpointManagementContextConfiguration.class,
		org.springframework.boot.actuate.autoconfigure.endpoint.web.servlet.WebMvcEndpointManagementContextConfiguration.class,
		org.springframework.boot.actuate.autoconfigure.security.servlet.SecurityRequestMatchersManagementContextConfiguration.class,
		org.springframework.boot.actuate.autoconfigure.web.jersey.JerseySameManagementContextConfiguration.class,
		org.springframework.boot.actuate.autoconfigure.web.jersey.JerseyChildManagementContextConfiguration.class,
		org.springframework.boot.actuate.autoconfigure.web.reactive.ReactiveManagementChildContextConfiguration.class,

		ServletEndpointRegistrar.class,
		AbstractWebMvcEndpointHandlerMapping.class,	
		// These collected from agent needs against actuator sample
		org.reactivestreams.Publisher.class,
		AuditEventRepository.class, AuditEventsEndpoint.class,
		ConditionsReportEndpoint.class,EndpointAutoConfiguration.class,
		ExposeExcludePropertyEndpointFilter.class,
		HealthContributorAutoConfiguration.class,
		HealthEndpointProperties.class,
		HealthProperties.class,
		PropertiesMeterFilter.class,
		PropertiesConfigAdapter.class,
		SimplePropertiesConfigAdapter.class,
		WebFluxEndpointManagementContextConfiguration.class,
		PathMapper.class,
		DefaultHealthContributorRegistry.class,
		DefaultReactiveHealthContributorRegistry.class,
		HealthAggregator.class,
		HealthComponent.class,
		HealthContributorRegistry.class,
		HealthEndpointGroups.class,
		ManagementContextConfiguration.class,
		ManagementContextFactory.class,
		ManagementContextType.class,
		MappingsEndpointAutoConfiguration.class,
		EnableManagementContext.class,
		ManagementContextConfigurationImportSelector.class,
		SameManagementContextConfiguration.class,
		ManagementPortType.class,
		BeansEndpoint.class,
		CachesEndpoint.class,
		ShutdownEndpoint.class,
		ConfigurationPropertiesReportEndpoint.class,
		EndpointFilter.class,
		EndpointsSupplier.class,
		Endpoint.class,
		EndpointConverter.class,
		EndpointDiscoverer.class,
		EndpointExtension.class,
		FilteredEndpoint.class,
		ReadOperation.class,
		OperationInvokerAdvisor.class,
		ParameterValueMapper.class,
		ConversionServiceParameterValueMapper.class,
		CachingOperationInvokerAdvisor.class,
		EndpointMediaTypes.class,
		PathMappedEndpoints.class,
		WebEndpointsSupplier.class,
		ControllerEndpointDiscoverer.class,
		ControllerEndpointsSupplier.class,
		EndpointWebExtension.class,
		WebEndpoint.class,
		WebEndpointDiscoverer.class,
		AbstractWebFluxEndpointHandlerMapping.class,
		EnvironmentEndpoint.class,
		AbstractHealthAggregator.class,
		AbstractHealthIndicator.class,
		CompositeHealth.class,
		ContributorRegistry.class,
		HealthIndicator.class,
		HealthIndicatorRegistry.class,
		HttpCodeStatusMapper.class,
		NamedContributors.class,
		OrderedHealthAggregator.class,
		PingHealthIndicator.class,
		ReactiveHealthContributorRegistry.class,
		ReactiveHealthEndpointWebExtension.class,
		ReactiveHealthIndicatorRegistry.class,
		SimpleHttpCodeStatusMapper.class,
		SimpleStatusAggregator.class,
		Status.class,
		StatusAggregator.class,
		SystemHealth.class,
		EnvironmentInfoContributor.class,
		InfoContributor.class,
		InfoEndpoint.class,
		LogFileWebEndpoint.class,
		LoggersEndpoint.class,
		HeapDumpWebEndpoint.class,
		ThreadDumpEndpoint.class,
		MetricsEndpoint.class,
		DefaultWebClientExchangeTagsProvider.class,
		MetricsWebClientCustomizer.class,
		WebClientExchangeTagsProvider.class,
		DefaultWebFluxTagsProvider.class,
		MetricsWebFilter.class,
		WebFluxTagsProvider.class,
		ScheduledTasksEndpoint.class,
		DiskSpaceHealthIndicator.class,
		HttpTraceEndpoint.class,

		// Additional for webmvc actuator sample
		WebMvcEndpointHandlerMapping.class,
		ControllerEndpointHandlerMapping.class
	}, typeNames = {
			
		// TODO this next one, actuator looks like the first thing pushing on it but surely it isn't the only user and this should have a different trigger?
		"org.springframework.context.annotation.ConfigurationClassParser$DefaultDeferredImportSelectorGroup",

		"org.springframework.boot.actuate.autoconfigure.health.AutoConfiguredHealthEndpointGroups",
		"org.springframework.core.LocalVariableTableParameterNameDiscoverer",
		"org.springframework.boot.actuate.autoconfigure.endpoint.web.jersey.JerseyWebEndpointManagementContextConfiguration",
		"org.springframework.boot.actuate.autoconfigure.web.servlet.ServletManagementChildContextConfiguration",
		"org.springframework.boot.actuate.autoconfigure.web.servlet.WebMvcEndpointChildContextConfiguration",

		"org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping$LinksHandler",
		"org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping$OperationHandler",
		"org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping$WebMvcLinksHandler",
		
		// these are from agent output for actuator sample
		"org.springframework.boot.actuate.health.HealthEndpointGroups$1",
		"org.springframework.boot.actuate.health.HealthEndpointSupport",
		"org.springframework.boot.actuate.health.DefaultContributorRegistry",
		"org.springframework.boot.actuate.autoconfigure.health.HealthEndpointWebExtensionConfiguration",
		"org.springframework.boot.actuate.autoconfigure.health.ReactiveHealthContributorRegistryReactiveHealthIndicatorRegistryAdapter",
		"org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryConfiguration",
		"org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryPostProcessor",
		"org.springframework.boot.actuate.autoconfigure.metrics.NoOpMeterRegistryConfiguration",
		"org.springframework.boot.actuate.autoconfigure.metrics.web.client.RestTemplateMetricsConfiguration",
		"org.springframework.boot.actuate.autoconfigure.web.reactive.ReactiveManagementContextFactory",
		"org.springframework.boot.actuate.endpoint.web.annotation.WebEndpointFilter",
		"org.springframework.boot.actuate.endpoint.web.reactive.WebFluxEndpointHandlerMapping$WebFluxLinksHandler",
		"org.springframework.boot.actuate.endpoint.web.reactive.AbstractWebFluxEndpointHandlerMapping$LinksHandler",
		"org.springframework.boot.actuate.endpoint.web.reactive.AbstractWebFluxEndpointHandlerMapping$ReadOperationHandler",
		"org.springframework.boot.actuate.endpoint.web.reactive.AbstractWebFluxEndpointHandlerMapping$WriteOperationHandler",
		"org.springframework.boot.actuate.autoconfigure.endpoint.web.MappingWebEndpointPathMapper",
		"org.springframework.boot.actuate.autoconfigure.health.AutoConfiguredHealthContributorRegistry",
		"org.springframework.boot.actuate.autoconfigure.health.AutoConfiguredReactiveHealthContributorRegistry",
		"org.springframework.boot.actuate.autoconfigure.health.HealthContributorRegistryHealthIndicatorRegistryAdapter"
	})})
public class Hints implements NativeImageConfiguration {
}