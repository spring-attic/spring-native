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

package org.demo.configserver;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.cloud.config.server.config.CompositeConfiguration;
import org.springframework.cloud.config.server.config.ConfigServerAutoConfiguration;
import org.springframework.cloud.config.server.config.ConfigServerConfiguration;
import org.springframework.cloud.config.server.config.ConfigServerEncryptionConfiguration;
import org.springframework.cloud.config.server.config.ConfigServerMvcConfiguration;
import org.springframework.cloud.config.server.config.ConfigServerProperties;
import org.springframework.cloud.config.server.config.ResourceRepositoryConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Olga Maciaszek-Sharma
 */
@SpringBootTest
class ConfigServerApplicationTests {

	@Autowired
	ConfigurableApplicationContext applicationContext;

	@Test
	void shouldLoadContext() {
		AssertableApplicationContext context = AssertableApplicationContext
				.get(() -> applicationContext);
		assertThat(context).hasSingleBean(ConfigServerConfiguration.class)
				.hasSingleBean(CompositeConfiguration.class)
				.hasSingleBean(ResourceRepositoryConfiguration.class)
				.hasSingleBean(ConfigServerEncryptionConfiguration.class)
				.hasSingleBean(ConfigServerMvcConfiguration.class)
				.hasSingleBean(ConfigServerAutoConfiguration.class)
				.hasSingleBean(ConfigServerProperties.class);
	}

}