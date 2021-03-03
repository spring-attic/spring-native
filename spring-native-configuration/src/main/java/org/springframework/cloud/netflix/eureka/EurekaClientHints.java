package org.springframework.cloud.netflix.eureka;

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
public class EurekaClientHints implements NativeConfiguration {
}
