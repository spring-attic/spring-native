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

import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;

// Hitting /configprops endpoint
@NativeImageHint(trigger = ConfigurationPropertiesReportEndpointAutoConfiguration.class, typeInfos = { 
	@TypeInfo(types = {
			io.micrometer.core.instrument.simple.CountingMode.class,
			org.springframework.boot.actuate.autoconfigure.info.InfoContributorProperties.Git.class,
			org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties.Servlet.class,
			org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ApplicationConfigurationProperties.class,
			org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ConfigurationPropertiesBeanDescriptor.class,
			org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ContextConfigurationProperties.class,
			org.springframework.boot.actuate.info.InfoPropertiesInfoContributor.Mode.class,
			org.springframework.boot.actuate.metrics.AutoTimer.class,
			org.springframework.boot.web.server.Shutdown.class,
	}),
	@TypeInfo(types= ConfigurationPropertiesReportEndpoint.class,access=AccessBits.LOAD_AND_CONSTRUCT|AccessBits.PUBLIC_METHODS)
})
public class ConfigurationPropertiesReportEndpointAutoConfigurationHints implements NativeImageConfiguration {

}