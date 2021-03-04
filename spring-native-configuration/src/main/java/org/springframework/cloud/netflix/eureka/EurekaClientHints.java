package org.springframework.cloud.netflix.eureka;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.DataCenterInfo;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.MyDataCenterInfo;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.converters.jackson.builder.ApplicationsJacksonBuilder;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;

import org.springframework.cloud.netflix.eureka.http.EurekaApplications;
import org.springframework.cloud.netflix.eureka.loadbalancer.EurekaLoadBalancerClientConfiguration;
import org.springframework.cloud.netflix.eureka.loadbalancer.LoadBalancerEurekaAutoConfiguration;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = LoadBalancerEurekaAutoConfiguration.class, types = {
		@TypeHint(types = {
				EurekaLoadBalancerClientConfiguration.class
		}, access = AccessBits.ALL)
})
@NativeHint(trigger = EurekaClientAutoConfiguration.class, types = {
		@TypeHint(types = {
				ApplicationInfoManager.class, EurekaInstanceConfig.class, DiscoveryClient.class,
				EurekaApplications.class, Applications.class, Application.class, ApplicationsJacksonBuilder.class,
				DataCenterInfo.class, DataCenterInfo.Name.class, MyDataCenterInfo.class
		}, typeNames = "com.netflix.discovery.DiscoveryClient$EurekaTransport", access = AccessBits.ALL)
})
public class EurekaClientHints implements NativeConfiguration {
}
