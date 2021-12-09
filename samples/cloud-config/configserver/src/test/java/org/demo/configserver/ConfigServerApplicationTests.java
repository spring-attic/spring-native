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
		assertThat(context).hasSingleBean(ConfigServerConfiguration.class);
		assertThat(context).hasSingleBean(CompositeConfiguration.class);
		assertThat(context).hasSingleBean(ResourceRepositoryConfiguration.class);
		assertThat(context).hasSingleBean(ConfigServerEncryptionConfiguration.class);
		assertThat(context).hasSingleBean(ConfigServerMvcConfiguration.class);
		assertThat(context).hasSingleBean(ConfigServerAutoConfiguration.class);
		assertThat(context).hasSingleBean(ConfigServerProperties.class);
	}

}