package com.example.discoveryclient;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.cloud.client.CommonsClientAutoConfiguration;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClientConfiguration;
import org.springframework.cloud.netflix.eureka.loadbalancer.LoadBalancerEurekaAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Olga Maciaszek-Sharma
 */
@SpringBootTest
public class DiscoveryClientApplicationTests {

	@Autowired
	private ConfigurableApplicationContext applicationContext;

	@Test
	void shouldLoadContext() {
		AssertableApplicationContext context = AssertableApplicationContext
				.get(() -> applicationContext);
		assertThat(context).getBeans(DiscoveryClient.class).
				containsOnlyKeys("compositeDiscoveryClient", "simpleDiscoveryClient", "discoveryClient");
		assertThat(context).getBean("discoveryClient")
				.isInstanceOf(EurekaDiscoveryClient.class);
		assertThat(context).hasSingleBean(EurekaDiscoveryClientConfiguration.class);
		assertThat(context).hasSingleBean(CommonsClientAutoConfiguration.class);
		assertThat(context).hasSingleBean(LoadBalancerEurekaAutoConfiguration.class);
	}
}
