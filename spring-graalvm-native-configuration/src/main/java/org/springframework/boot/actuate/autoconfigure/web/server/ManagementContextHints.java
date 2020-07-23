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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.actuate.audit.AuditEventsEndpoint;
import org.springframework.boot.actuate.autoconfigure.audit.AuditEventsEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.beans.BeansEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.cache.CachesEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint;
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint.ApplicationConditionEvaluation;
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint.ContextConditionEvaluation;
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint.MessageAndCondition;
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint.MessageAndConditions;
import org.springframework.boot.actuate.autoconfigure.context.properties.ConfigurationPropertiesReportEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.ExposeExcludePropertyEndpointFilter;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.ServletEndpointManagementContextConfiguration.WebMvcServletEndpointManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.reactive.WebFluxEndpointManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.env.EnvironmentEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.health.HealthProperties;
import org.springframework.boot.actuate.autoconfigure.info.InfoEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.PropertiesMeterFilter;
import org.springframework.boot.actuate.autoconfigure.metrics.export.properties.PropertiesConfigAdapter;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimplePropertiesConfigAdapter;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextFactory;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextType;
import org.springframework.boot.actuate.autoconfigure.web.mappings.MappingsEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.reactive.ReactiveManagementChildContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration.SameManagementContextConfiguration;
import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.boot.actuate.beans.BeansEndpoint.ApplicationBeans;
import org.springframework.boot.actuate.beans.BeansEndpoint.BeanDescriptor;
import org.springframework.boot.actuate.beans.BeansEndpoint.ContextBeans;
import org.springframework.boot.actuate.cache.CachesEndpoint;
import org.springframework.boot.actuate.context.ShutdownEndpoint;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.actuate.endpoint.EndpointFilter;
import org.springframework.boot.actuate.endpoint.EndpointsSupplier;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.EndpointConverter;
import org.springframework.boot.actuate.endpoint.annotation.EndpointDiscoverer;
import org.springframework.boot.actuate.endpoint.annotation.EndpointExtension;
import org.springframework.boot.actuate.endpoint.annotation.FilteredEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.Selector.Match;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
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
import org.springframework.graalvm.type.AccessBits;
import org.springframework.graalvm.type.CompilationHint;
import org.springframework.graalvm.type.TypeSystem;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.std.ClassSerializer;

@NativeImageHint(trigger = ManagementContextConfigurationImportSelector.class, typeInfos= {
		// TODO - hard to work it out...
})
// TODO what is the 'entrypoint' trigger configuration to actuator?
@NativeImageHint(trigger = EndpointAutoConfiguration.class, typeInfos = { 
	@TypeInfo(types = {
		Endpoint.class,
		EndpointExtension.class,
		WebEndpoint.class,
		ReadOperation.class,
		WriteOperation.class,
		DeleteOperation.class,
		Selector.class,
		Match.class,
		EndpointFilter.class,
		EndpointsSupplier.class,
		Endpoint.class,
		EndpointConverter.class,
		EndpointDiscoverer.class,
		EndpointExtension.class,
		FilteredEndpoint.class,
		EndpointAutoConfiguration.class,

		// autoconfigure.web.server
		ManagementPortType.class,

		// actuate.endpoint.web.servlet
		org.springframework.boot.actuate.endpoint.web.servlet.ControllerEndpointHandlerMapping.class,

		// logging
		LogFileWebEndpoint.class,
		
		Link.class, // serialization problem for this if hit /actuator
		BeanSerializer.class,
		ApiVersion.class,
		HttpTraceRepository.class,
		EnableManagementContext.class,
		org.springframework.boot.actuate.autoconfigure.endpoint.web.servlet.WebMvcEndpointManagementContextConfiguration.class,
		org.springframework.boot.actuate.autoconfigure.security.servlet.SecurityRequestMatchersManagementContextConfiguration.class,

		ServletEndpointRegistrar.class,
		AbstractWebMvcEndpointHandlerMapping.class,	
		// These collected from agent needs against actuator sample
		org.reactivestreams.Publisher.class,
		ExposeExcludePropertyEndpointFilter.class,
		WebFluxEndpointManagementContextConfiguration.class,

		// MappingsEndpointAutoConfiguration.class,
		// EnableManagementContext.class,
		// ManagementContextConfigurationImportSelector.class,
		SameManagementContextConfiguration.class,
		ManagementPortType.class,
		ShutdownEndpoint.class,
		// ConfigurationPropertiesReportEndpoint.class,
		CloudPlatform.class,
		
		OperationInvokerAdvisor.class,
		ParameterValueMapper.class,
		ConversionServiceParameterValueMapper.class,

		AbstractWebFluxEndpointHandlerMapping.class,
		EnvironmentEndpoint.class,
		SimpleHttpCodeStatusMapper.class,
		SimpleStatusAggregator.class,

		// logging
		LogFileWebEndpoint.class,
		LoggersEndpoint.class,
		
		// management
		HeapDumpWebEndpoint.class,
		ThreadDumpEndpoint.class,
		
		ScheduledTasksEndpoint.class,
		DiskSpaceHealthIndicator.class,
		HttpTraceEndpoint.class,

		// Additional for webmvc actuator sample
		WebMvcEndpointHandlerMapping.class,
		ControllerEndpointHandlerMapping.class,

		org.springframework.boot.actuate.autoconfigure.web.reactive.ReactiveManagementChildContextConfiguration.class
	}, typeNames = {
		// TODO this next one, actuator looks like the first thing pushing on it but surely it isn't the only user and this should have a different trigger?
		"org.springframework.context.annotation.ConfigurationClassParser$DefaultDeferredImportSelectorGroup",
		"org.springframework.core.LocalVariableTableParameterNameDiscoverer",
	})
})

// Hitting ?
@NativeImageHint(trigger=WebEndpointAutoConfiguration.class, typeInfos = {
	@TypeInfo(types = {
		WebMvcServletEndpointManagementContextConfiguration.class,
		ControllerEndpointDiscoverer.class,
		ControllerEndpointsSupplier.class,
		org.springframework.boot.actuate.autoconfigure.endpoint.web.ServletEndpointManagementContextConfiguration.class,
		org.springframework.boot.actuate.autoconfigure.web.jersey.JerseySameManagementContextConfiguration.class,
		org.springframework.boot.actuate.autoconfigure.web.jersey.JerseyChildManagementContextConfiguration.class,
		ManagementContextType.class,
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
	}, typeNames = {
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

// Hitting /metrics endpoint
@NativeImageHint(trigger = MetricsEndpointAutoConfiguration.class, typeInfos = { 
	@TypeInfo(types = {
		MetricsEndpoint.class,
		PropertiesMeterFilter.class,
		PropertiesConfigAdapter.class,
		SimplePropertiesConfigAdapter.class,
		org.springframework.boot.actuate.metrics.MetricsEndpoint.ListNamesResponse.class,
		// TODO likely incomplete, not tested
	},typeNames = {
		"org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryConfiguration",
		"org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryPostProcessor",
		"org.springframework.boot.actuate.autoconfigure.metrics.NoOpMeterRegistryConfiguration",
		"org.springframework.boot.actuate.autoconfigure.metrics.web.client.RestTemplateMetricsConfiguration"
	})
})

// Hitting /caches endpoint
@NativeImageHint(trigger = CachesEndpointAutoConfiguration.class, typeInfos = { 
	@TypeInfo(types = {
		CachesEndpoint.class,
		CachingOperationInvokerAdvisor.class,
		// TODO likely incomplete, not tested
	})
})

// Hitting /auditevents endpoint
@NativeImageHint(trigger = AuditEventsEndpointAutoConfiguration.class, typeInfos = { 
	@TypeInfo(types = {
		AuditEventRepository.class, AuditEventsEndpoint.class,
		// TODO likely incomplete, not tested
	})
})

// Hitting /info endpoint
@NativeImageHint(trigger = InfoEndpointAutoConfiguration.class, typeInfos = { 
	@TypeInfo(types = {
		EnvironmentInfoContributor.class,
		InfoContributor.class,
		InfoEndpoint.class
	})
})


// Hitting /health endpoint
@NativeImageHint(trigger = HealthEndpointAutoConfiguration.class, typeInfos = { 
	@TypeInfo(types = {
		AbstractHealthAggregator.class,
		AbstractHealthIndicator.class,
		HealthIndicatorRegistry.class,
		OrderedHealthAggregator.class,
		PingHealthIndicator.class,
		ReactiveHealthContributorRegistry.class,
		ReactiveHealthEndpointWebExtension.class,
		ReactiveHealthIndicatorRegistry.class,
		Status.class,
		StatusAggregator.class,
		SystemHealth.class,
		CompositeHealth.class,
		ContributorRegistry.class,
		HealthIndicator.class,
		HttpCodeStatusMapper.class,
		NamedContributors.class,
		HealthComponent.class,	
		HealthEndpoint.class,
		HealthContributorAutoConfiguration.class,
		HealthEndpointProperties.class,
		HealthProperties.class,
		HealthContributor.class,
		DefaultHealthContributorRegistry.class,
		DefaultReactiveHealthContributorRegistry.class,
		HealthAggregator.class,
		HealthComponent.class,
		HealthContributorRegistry.class,
		HealthEndpointGroups.class
	},typeNames = {
		"org.springframework.boot.actuate.autoconfigure.health.AutoConfiguredHealthEndpointGroups",
		"org.springframework.boot.actuate.health.HealthEndpointGroups$1",
		"org.springframework.boot.actuate.health.HealthEndpointSupport",
		"org.springframework.boot.actuate.health.DefaultContributorRegistry",
		"org.springframework.boot.actuate.autoconfigure.health.HealthEndpointWebExtensionConfiguration",
		"org.springframework.boot.actuate.autoconfigure.health.ReactiveHealthContributorRegistryReactiveHealthIndicatorRegistryAdapter",
		"org.springframework.boot.actuate.autoconfigure.health.AutoConfiguredHealthContributorRegistry",
		"org.springframework.boot.actuate.autoconfigure.health.AutoConfiguredReactiveHealthContributorRegistry",
		"org.springframework.boot.actuate.autoconfigure.health.HealthContributorRegistryHealthIndicatorRegistryAdapter"
	})
})


// Hitting /beans endpoint
@NativeImageHint(trigger = BeansEndpointAutoConfiguration.class, typeInfos = { 
	@TypeInfo(types = {
		ClassSerializer.class, ApplicationBeans.class, BeanDescriptor.class, ContextBeans.class, BeansEndpoint.class
	})
})

// Hitting /env endpoint
@NativeImageHint(trigger = EnvironmentEndpointAutoConfiguration.class, typeInfos = { 
	@TypeInfo(types = {
		org.springframework.boot.actuate.env.EnvironmentEndpoint.EnvironmentDescriptor.class,
		org.springframework.boot.actuate.env.EnvironmentEndpoint.PropertySourceDescriptor.class,
		org.springframework.boot.actuate.env.EnvironmentEndpoint.PropertyValueDescriptor.class
	})
})

// Hitting /conditions endpoint
@NativeImageHint(trigger = ConditionsReportEndpointAutoConfiguration.class, typeInfos = { 
	@TypeInfo(types = {
		ConditionsReportEndpoint.class,
		ApplicationConditionEvaluation.class,
		ContextConditionEvaluation.class,
		MessageAndCondition.class,
		MessageAndConditions.class,
		MultiValueMap.class,
		LinkedMultiValueMap.class,
	})
})

// Hitting /mappings endpoint
@NativeImageHint(trigger = MappingsEndpointAutoConfiguration.class, typeInfos = { 
	@TypeInfo(types = {
		org.springframework.boot.actuate.web.mappings.HandlerMethodDescription.class,
		org.springframework.boot.actuate.web.mappings.MappingsEndpoint.ApplicationMappings.class,
		org.springframework.boot.actuate.web.mappings.MappingsEndpoint.ContextMappings.class,
		org.springframework.boot.actuate.web.mappings.servlet.DispatcherServletMappingDescription.class,
		org.springframework.boot.actuate.web.mappings.servlet.DispatcherServletMappingDetails.class,
		org.springframework.boot.actuate.web.mappings.servlet.FilterRegistrationMappingDescription.class,
		org.springframework.boot.actuate.web.mappings.servlet.RegistrationMappingDescription.class,
		org.springframework.boot.actuate.web.mappings.servlet.RequestMappingConditionsDescription.class,
		org.springframework.boot.actuate.web.mappings.servlet.RequestMappingConditionsDescription.MediaTypeExpressionDescription.class,
		org.springframework.boot.actuate.web.mappings.servlet.ServletRegistrationMappingDescription.class,
		org.springframework.web.bind.annotation.RequestMethod.class
	})
})

// Hitting /configprops endpoint
@NativeImageHint(trigger = ConfigurationPropertiesReportEndpointAutoConfiguration.class, typeInfos = { 
	@TypeInfo(types = {
			com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.class,
			com.fasterxml.jackson.annotation.JsonInclude.Include.class,
			com.fasterxml.jackson.annotation.PropertyAccessor.class,
			com.fasterxml.jackson.core.JsonGenerator.Feature.class,
			com.fasterxml.jackson.core.JsonParser.Feature.class,
			com.fasterxml.jackson.databind.DeserializationFeature.class,
			com.fasterxml.jackson.databind.MapperFeature.class,
			com.fasterxml.jackson.databind.SerializationFeature.class,
			com.fasterxml.jackson.databind.cfg.ConfigFeature.class,
			com.fasterxml.jackson.databind.ser.BeanSerializerModifier[].class,
			com.fasterxml.jackson.databind.ser.std.FileSerializer.class,
			io.micrometer.core.instrument.simple.CountingMode.class,
			java.io.File.class,
			java.lang.Character.class,
			java.lang.Cloneable.class,
			java.lang.Comparable.class,
			java.nio.charset.Charset.class,
			java.util.Locale.class,
			org.springframework.boot.actuate.autoconfigure.health.HealthProperties.Show.class,
			org.springframework.boot.actuate.autoconfigure.health.HealthProperties.Status.class,
			org.springframework.boot.actuate.autoconfigure.info.InfoContributorProperties.Git.class,
			org.springframework.boot.actuate.autoconfigure.metrics.AutoTimeProperties.class,
			org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Distribution.class,
			org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Web.class,
			org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Web.Client.class,
			org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Web.Client.ClientRequest.class,
			org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Web.Server.class,
			org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Web.Server.ServerRequest.class,
			org.springframework.boot.actuate.autoconfigure.metrics.ServiceLevelObjectiveBoundary[].class,
			org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties.Servlet.class,
			org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ApplicationConfigurationProperties.class,
			org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ConfigurationPropertiesBeanDescriptor.class,
			org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ContextConfigurationProperties.class,
			org.springframework.boot.actuate.info.InfoPropertiesInfoContributor.Mode.class,
			org.springframework.boot.actuate.metrics.AutoTimer.class,
			org.springframework.boot.autoconfigure.info.ProjectInfoProperties.Build.class,
			org.springframework.boot.autoconfigure.info.ProjectInfoProperties.Git.class,
			org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties.Jwt.class,
			org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties.Opaquetoken.class,
			org.springframework.boot.autoconfigure.task.TaskExecutionProperties.Pool.class,
			org.springframework.boot.autoconfigure.task.TaskExecutionProperties.Shutdown.class,
			org.springframework.boot.autoconfigure.task.TaskSchedulingProperties.Pool.class,
			org.springframework.boot.autoconfigure.task.TaskSchedulingProperties.Shutdown.class,
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
			org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties.Async.class,
			org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties.Contentnegotiation.class,
			org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties.Format.class,
			org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties.LocaleResolver.class,
			org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties.Pathmatch.class,
			org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties.Servlet.class,
			org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties.View.class,
			org.springframework.boot.web.server.Shutdown.class,
			org.springframework.core.io.AbstractFileResolvingResource.class,
			org.springframework.core.io.AbstractResource.class,
			org.springframework.core.io.ClassPathResource.class,
			org.springframework.core.io.InputStreamSource.class,
			org.springframework.core.io.Resource.class,
			org.springframework.util.unit.DataSize.class
	})
})
public class ManagementContextHints implements NativeImageConfiguration {

	@Override
	public List<CompilationHint> computeHints(TypeSystem typeSystem) {
		try {
			List<CompilationHint> hints = new ArrayList<>();
			// There are many types in the actuator jar so the check for whether they are
			// truly required isn't
			// trivial. (For example actuator jar contains
			// AbstractWebFluxEndpointHandlerMapping but this may
			// not be a webflux application).

			// Similar to check in OnWebApplicationCondition
			boolean isWebfluxApplication = typeSystem.resolveName("org.springframework.web.reactive.HandlerResult",
					true) != null;
			if (isWebfluxApplication) {
				CompilationHint ch = new CompilationHint();
				ch.setTargetType(ManagementContextConfigurationImportSelector.class.getName());
				ch.addDependantType(AbstractWebFluxEndpointHandlerMapping.class.getName(), AccessBits.ALL);
				ch.addDependantType(ControllerEndpointHandlerMapping.class.getName(), AccessBits.ALL);
				ch.addDependantType(WebFluxEndpointHandlerMapping.class.getName(), AccessBits.ALL);
				ch.addDependantType(
						"org.springframework.boot.actuate.endpoint.web.reactive.WebFluxEndpointHandlerMapping$WebFluxLinksHandler",
						AccessBits.ALL);
				ch.addDependantType(
						"org.springframework.boot.actuate.endpoint.web.reactive.AbstractWebFluxEndpointHandlerMapping$LinksHandler",
						AccessBits.ALL);
				ch.addDependantType(
						"org.springframework.boot.actuate.endpoint.web.reactive.AbstractWebFluxEndpointHandlerMapping$ReadOperationHandler",
						AccessBits.ALL);
				ch.addDependantType(
						"org.springframework.boot.actuate.endpoint.web.reactive.AbstractWebFluxEndpointHandlerMapping$WriteOperationHandler",
						AccessBits.ALL);
				ch.addDependantType(
						"org.springframework.boot.actuate.autoconfigure.web.reactive.ReactiveManagementContextFactory",
						AccessBits.ALL);
				ch.addDependantType(DefaultWebClientExchangeTagsProvider.class, AccessBits.ALL);
				ch.addDependantType(WebClientExchangeTagsProvider.class, AccessBits.ALL);
				ch.addDependantType(MetricsWebFilter.class, AccessBits.ALL);
				ch.addDependantType(DefaultWebFluxTagsProvider.class, AccessBits.ALL);
				ch.addDependantType(WebFluxTagsProvider.class, AccessBits.ALL);
				ch.addDependantType(MetricsWebClientCustomizer.class, AccessBits.ALL);
				ch.addDependantType(WebFluxEndpointManagementContextConfiguration.class, AccessBits.ALL);
				ch.addDependantType(ReactiveManagementChildContextConfiguration.class, AccessBits.ALL);
				hints.add(ch);
			}
			return hints;
		} catch (NoClassDefFoundError ncdfe) {
			// these hints aren't useful because types are not around
			return Collections.emptyList();
		}
	}

}