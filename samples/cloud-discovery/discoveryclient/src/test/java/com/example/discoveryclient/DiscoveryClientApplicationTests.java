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
		assertThat(context).hasSingleBean(EurekaDiscoveryClientConfiguration.class)
				.hasSingleBean(CommonsClientAutoConfiguration.class)
				.hasSingleBean(LoadBalancerEurekaAutoConfiguration.class);
	}
}
