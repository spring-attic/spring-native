package com.example.loadbalancerclient.config;

import java.util.Collections;
import java.util.List;

import reactor.core.publisher.Flux;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;

public class CustomLoadBalancerConfig {

	@Bean
	ServiceInstanceListSupplier serviceInstanceListSupplier() {
		return new CustomServiceInstanceListSupplier();
	}

}

class CustomServiceInstanceListSupplier implements ServiceInstanceListSupplier {

	@Override
	public String getServiceId() {
		return "custom-test-service";
	}

	@Override
	public Flux<List<ServiceInstance>> get() {
		return Flux.just(Collections
				.singletonList(new DefaultServiceInstance("custom-test-service-1", "custom-test-service",
						"localhost", 8081, false)));
	}
}
