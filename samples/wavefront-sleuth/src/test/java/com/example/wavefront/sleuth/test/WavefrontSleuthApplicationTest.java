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
package com.example.wavefront.sleuth.test;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.cloud.sleuth.autoconfig.TraceConfiguration;
import org.springframework.cloud.sleuth.autoconfig.brave.SleuthPropagationProperties;
import org.springframework.cloud.sleuth.autoconfig.brave.SleuthProperties;
import org.springframework.cloud.sleuth.autoconfig.brave.instrument.web.BraveHttpConfiguration;
import org.springframework.cloud.sleuth.autoconfig.instrument.messaging.SleuthMessagingProperties;
import org.springframework.cloud.sleuth.autoconfig.instrument.web.TraceWebAutoConfiguration;
import org.springframework.cloud.sleuth.autoconfig.instrument.web.client.TraceWebAsyncClientAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class WavefrontSleuthApplicationTest {

	@Autowired
	ConfigurableApplicationContext applicationContext;

	@Test
	public void contextLoads() {
		AssertableApplicationContext context = AssertableApplicationContext.get(() -> applicationContext);
		assertThat(context).hasSingleBean(BraveHttpConfiguration.class)
				.hasSingleBean(TraceConfiguration.class)
				.hasSingleBean(SleuthPropagationProperties.class)
				.hasSingleBean(SleuthProperties.class)
				.hasSingleBean(SleuthMessagingProperties.class)
				.hasSingleBean(TraceWebAsyncClientAutoConfiguration.class)
				.hasSingleBean(TraceWebAutoConfiguration.class);
	}

}
