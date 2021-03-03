package org.springframework.cloud.loadbalancer;

import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClientSpecification;
import org.springframework.cloud.loadbalancer.config.LoadBalancerAutoConfiguration;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = LoadBalancerAutoConfiguration.class, types = {
		@TypeHint(types = {
				LoadBalancerClientSpecification.class
		}, access = AccessBits.ALL)
})
public class LoadBalancerHints implements NativeConfiguration {
}
