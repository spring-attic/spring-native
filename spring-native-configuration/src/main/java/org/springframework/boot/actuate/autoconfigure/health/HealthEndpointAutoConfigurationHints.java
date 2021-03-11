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

package org.springframework.boot.actuate.autoconfigure.health;

import org.springframework.boot.actuate.health.CompositeHealth;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.HealthEndpointGroups;
import org.springframework.boot.actuate.health.HealthEndpointWebExtension;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.HttpCodeStatusMapper;
import org.springframework.boot.actuate.health.NamedContributors;
import org.springframework.boot.actuate.health.ReactiveHealthEndpointWebExtension;
import org.springframework.boot.actuate.health.SimpleHttpCodeStatusMapper;
import org.springframework.boot.actuate.health.SimpleStatusAggregator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.actuate.health.StatusAggregator;
import org.springframework.boot.actuate.health.SystemHealth;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.hint.AccessBits;

// Hitting /health endpoint
@NativeHint(trigger = HealthEndpointAutoConfiguration.class, types = {
	@TypeHint(types = {
		ReactiveHealthEndpointWebExtension.class,
		HealthEndpointWebExtension.class,
	}),
	@TypeHint(types = {
		StatusAggregator.class,
		SimpleStatusAggregator.class,
		HttpCodeStatusMapper.class,
		SimpleHttpCodeStatusMapper.class,
	},access= AccessBits.LOAD_AND_CONSTRUCT | AccessBits.PUBLIC_METHODS),
	@TypeHint(types = {
		Status.class,
		SystemHealth.class,
		CompositeHealth.class,
		HealthIndicator.class,
		NamedContributors.class,
		HealthComponent.class,
		HealthProperties.class,
		HealthContributor.class,
		HealthComponent.class,
		org.springframework.boot.actuate.autoconfigure.health.HealthProperties.Show.class,
		org.springframework.boot.actuate.autoconfigure.health.HealthProperties.Status.class,
		HealthEndpointGroups.class
	},typeNames = {
		"org.springframework.boot.actuate.health.HealthEndpointGroups$1",
		"org.springframework.boot.actuate.health.HealthEndpointSupport",
		"org.springframework.boot.actuate.autoconfigure.health.HealthEndpointWebExtensionConfiguration",
		"org.springframework.boot.actuate.autoconfigure.health.ReactiveHealthContributorRegistryReactiveHealthIndicatorRegistryAdapter",
		"org.springframework.boot.actuate.autoconfigure.health.HealthContributorRegistryHealthIndicatorRegistryAdapter"
	},access= AccessBits.LOAD_AND_CONSTRUCT | AccessBits.PUBLIC_METHODS),
	@TypeHint(
		types = HealthEndpoint.class,
		typeNames = "org.springframework.boot.actuate.autoconfigure.health.AutoConfiguredHealthEndpointGroups",
		access = AccessBits.LOAD_AND_CONSTRUCT_AND_PUBLIC_METHODS)
,@TypeHint(types = Health.class, access = AccessBits.LOAD_AND_CONSTRUCT | AccessBits.DECLARED_FIELDS | AccessBits.DECLARED_METHODS)})
public class HealthEndpointAutoConfigurationHints implements NativeConfiguration {
}