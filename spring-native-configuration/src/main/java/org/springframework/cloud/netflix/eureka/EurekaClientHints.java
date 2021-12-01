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

package org.springframework.cloud.netflix.eureka;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.LeaseInfo;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.converters.jackson.DataCenterTypeInfoResolver;
import com.netflix.discovery.converters.jackson.builder.ApplicationsJacksonBuilder;

import org.springframework.cloud.netflix.eureka.loadbalancer.EurekaLoadBalancerClientConfiguration;
import org.springframework.cloud.netflix.eureka.loadbalancer.LoadBalancerEurekaAutoConfiguration;
import org.springframework.nativex.hint.FieldHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = EurekaClientAutoConfiguration.class, types = {
		@TypeHint(types = {
				ApplicationsJacksonBuilder.class,
				InstanceInfo.class,
				DataCenterTypeInfoResolver.class,
				LeaseInfo.class
		}, access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS, TypeAccess.PUBLIC_METHODS}),
		@TypeHint(types = DiscoveryClient.class, fields = @FieldHint(name = "eurekaTransport"))
})
@NativeHint(trigger = EurekaDiscoveryClientConfiguration.class,
		options = "--enable-http")
@NativeHint(trigger = LoadBalancerEurekaAutoConfiguration.class,
		types = @TypeHint(types = EurekaLoadBalancerClientConfiguration.class, access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS, TypeAccess.PUBLIC_METHODS}))
public class EurekaClientHints implements NativeConfiguration {
}
