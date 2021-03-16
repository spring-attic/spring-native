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

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.DataCenterInfo;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.LeaseInfo;
import com.netflix.appinfo.MyDataCenterInfo;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.converters.jackson.DataCenterTypeInfoResolver;
import com.netflix.discovery.converters.jackson.builder.ApplicationsJacksonBuilder;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;

import org.springframework.cloud.netflix.eureka.http.EurekaApplications;
import org.springframework.cloud.netflix.eureka.loadbalancer.EurekaLoadBalancerClientConfiguration;
import org.springframework.cloud.netflix.eureka.loadbalancer.LoadBalancerEurekaAutoConfiguration;
import org.springframework.cloud.netflix.eureka.reactive.EurekaReactiveDiscoveryClientConfiguration;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

// TODO Not sure AccessBits.ALL is needed on these, to be checked
@NativeHint(trigger = LoadBalancerEurekaAutoConfiguration.class, types = {
		@TypeHint(types = {
				EurekaLoadBalancerClientConfiguration.class,
				EurekaReactiveDiscoveryClientConfiguration.class
		}, access = AccessBits.ALL)
})
@NativeHint(trigger = EurekaClientAutoConfiguration.class, types = {
		@TypeHint(types = {
				ApplicationInfoManager.class,
				EurekaInstanceConfig.class,
				DiscoveryClient.class,
				EurekaApplications.class,
				Applications.class,
				Application.class,
				ApplicationsJacksonBuilder.class,
				DataCenterInfo.class,
				DataCenterInfo.Name.class,
				MyDataCenterInfo.class,
				InstanceInfo.class,
				DataCenterTypeInfoResolver.class,
				LeaseInfo.class
		}, typeNames = {
				"com.netflix.discovery.DiscoveryClient$EurekaTransport",
				"com.netflix.appinfo.InstanceInfo$PortWrapper"
		}, access = AccessBits.ALL)
})
public class EurekaClientHints implements NativeConfiguration {
}
