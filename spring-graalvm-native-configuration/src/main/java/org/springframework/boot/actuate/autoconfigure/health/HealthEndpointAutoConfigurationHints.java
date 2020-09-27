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
package org.springframework.boot.actuate.autoconfigure.health;

import org.springframework.boot.actuate.health.CompositeHealth;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.HealthEndpointGroups;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.HttpCodeStatusMapper;
import org.springframework.boot.actuate.health.NamedContributors;
import org.springframework.boot.actuate.health.PingHealthIndicator;
import org.springframework.boot.actuate.health.ReactiveHealthEndpointWebExtension;
import org.springframework.boot.actuate.health.SimpleHttpCodeStatusMapper;
import org.springframework.boot.actuate.health.SimpleStatusAggregator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.actuate.health.StatusAggregator;
import org.springframework.boot.actuate.health.SystemHealth;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;

// Hitting /health endpoint
@NativeImageHint(trigger = HealthEndpointAutoConfiguration.class, typeInfos = { 
	@TypeInfo(types = {
		SimpleHttpCodeStatusMapper.class,
		SimpleStatusAggregator.class,
		PingHealthIndicator.class,
		ReactiveHealthEndpointWebExtension.class,
		Status.class,
		StatusAggregator.class,
		SystemHealth.class,
		CompositeHealth.class,
		HealthIndicator.class,
		HttpCodeStatusMapper.class,
		NamedContributors.class,
		HealthComponent.class,	
		HealthContributorAutoConfiguration.class,
		HealthEndpointProperties.class,
		HealthProperties.class,
		HealthContributor.class,
		HealthAggregator.class,
		HealthComponent.class,
		org.springframework.boot.actuate.autoconfigure.health.HealthProperties.Show.class,
		org.springframework.boot.actuate.autoconfigure.health.HealthProperties.Status.class,
		HealthEndpointGroups.class
	},typeNames = {
		"org.springframework.boot.actuate.health.HealthEndpointGroups$1",
		"org.springframework.boot.actuate.health.HealthEndpointSupport",
//		"org.springframework.boot.actuate.health.DefaultContributorRegistry",
		"org.springframework.boot.actuate.autoconfigure.health.HealthEndpointWebExtensionConfiguration",
		"org.springframework.boot.actuate.autoconfigure.health.ReactiveHealthContributorRegistryReactiveHealthIndicatorRegistryAdapter",
//		"org.springframework.boot.actuate.autoconfigure.health.AutoConfiguredHealthContributorRegistry",
//		"org.springframework.boot.actuate.autoconfigure.health.AutoConfiguredReactiveHealthContributorRegistry",
		"org.springframework.boot.actuate.autoconfigure.health.HealthContributorRegistryHealthIndicatorRegistryAdapter"
	}),
//	@TypeInfo(types = {
//		HealthContributorRegistry.class, DefaultHealthContributorRegistry.class,
//		AutoConfiguredHealthContributorRegistry.class,
//		ReactiveHealthIndicatorRegistry.class,
//		AutoConfiguredReactiveHealthContributorRegistry.class,
//		DefaultReactiveHealthContributorRegistry.class,
//		ContributorRegistry.class, ReactiveHealthContributorRegistry.class,
//		},access=AccessBits.LOAD_AND_CONSTRUCT),
	@TypeInfo(
		types = HealthEndpoint.class,
		typeNames = "org.springframework.boot.actuate.autoconfigure.health.AutoConfiguredHealthEndpointGroups",
		access=AccessBits.LOAD_AND_CONSTRUCT_AND_PUBLIC_METHODS)
})
public class HealthEndpointAutoConfigurationHints implements NativeImageConfiguration {
}