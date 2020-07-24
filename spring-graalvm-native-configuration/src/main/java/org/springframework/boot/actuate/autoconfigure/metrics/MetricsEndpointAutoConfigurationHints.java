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
package org.springframework.boot.actuate.autoconfigure.metrics;

import org.springframework.boot.actuate.autoconfigure.metrics.export.properties.PropertiesConfigAdapter;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimplePropertiesConfigAdapter;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;

// Hitting /metrics endpoint
@NativeImageHint(trigger = MetricsEndpointAutoConfiguration.class, typeInfos = { 
	@TypeInfo(types = {
		MetricsEndpoint.class,
		PropertiesMeterFilter.class,
		PropertiesConfigAdapter.class,
		SimplePropertiesConfigAdapter.class,
		org.springframework.boot.actuate.metrics.MetricsEndpoint.ListNamesResponse.class,
		org.springframework.boot.actuate.autoconfigure.metrics.AutoTimeProperties.class,
		org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Distribution.class,
		org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Web.class,
		org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Web.Client.class,
		org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Web.Client.ClientRequest.class,
		org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Web.Server.class,
		org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Web.Server.ServerRequest.class,
		org.springframework.boot.actuate.autoconfigure.metrics.ServiceLevelObjectiveBoundary[].class,
		// TODO likely incomplete, not tested
	},typeNames = {
		"org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryConfiguration",
		"org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryPostProcessor",
		"org.springframework.boot.actuate.autoconfigure.metrics.NoOpMeterRegistryConfiguration",
		"org.springframework.boot.actuate.autoconfigure.metrics.web.client.RestTemplateMetricsConfiguration"
	})
})
public class MetricsEndpointAutoConfigurationHints implements NativeImageConfiguration {
}