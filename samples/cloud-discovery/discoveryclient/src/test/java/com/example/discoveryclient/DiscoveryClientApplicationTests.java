package com.example.discoveryclient;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.CommonsClientAutoConfiguration;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClientConfiguration;
import org.springframework.cloud.netflix.eureka.loadbalancer.LoadBalancerEurekaAutoConfiguration;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Olga Maciaszek-Sharma
 */
@SpringBootTest
public class DiscoveryClientApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	void shouldLoadContext() {
		assertThat(applicationContext.getBeansOfType(DiscoveryClient.class))
				.containsOnlyKeys("compositeDiscoveryClient", "simpleDiscoveryClient", "discoveryClient");
		assertThat(applicationContext.getBean("discoveryClient").getClass())
				.isEqualTo(EurekaDiscoveryClient.class);
		assertThat(applicationContext.getBean(EurekaDiscoveryClientConfiguration.class))
				.isNotNull();
		assertThat(applicationContext.getBean(CommonsClientAutoConfiguration.class))
				.isNotNull();
		assertThat(applicationContext.getBean(LoadBalancerEurekaAutoConfiguration.class))
				.isNotNull();
	}
}
