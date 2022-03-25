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

package org.springframework.boot.actuate.autoconfigure.metrics;

import org.springframework.boot.actuate.autoconfigure.metrics.export.properties.PropertiesConfigAdapter;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimplePropertiesConfigAdapter;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

// Hitting /metrics endpoint - needs tests
@NativeHint(trigger = MetricsEndpointAutoConfiguration.class, types = {
	@TypeHint(types = {
		PropertiesConfigAdapter.class,
		SimplePropertiesConfigAdapter.class,
		org.springframework.boot.actuate.autoconfigure.metrics.ServiceLevelObjectiveBoundary.class,
		org.springframework.boot.actuate.autoconfigure.metrics.ServiceLevelObjectiveBoundary[].class,
	}),
	@TypeHint(types = {
		MetricsEndpoint.class,
		MetricsEndpoint.MetricResponse.class,
		MetricsEndpoint.Sample.class,
		MetricsEndpoint.AvailableTag.class, 
		MetricsEndpoint.ListNamesResponse.class
	}, access = { TypeAccess.DECLARED_METHODS, TypeAccess.DECLARED_FIELDS})
})
public class MetricsEndpointAutoConfigurationHints implements NativeConfiguration {
}