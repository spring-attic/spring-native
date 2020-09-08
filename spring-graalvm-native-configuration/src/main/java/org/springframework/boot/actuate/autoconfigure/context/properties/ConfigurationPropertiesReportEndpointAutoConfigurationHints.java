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
package org.springframework.boot.actuate.autoconfigure.context.properties;

import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;

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

			org.springframework.boot.actuate.autoconfigure.info.InfoContributorProperties.Git.class,
			org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties.Servlet.class,
			org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.class,
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
public class ConfigurationPropertiesReportEndpointAutoConfigurationHints implements NativeImageConfiguration {

}