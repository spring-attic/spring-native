package org.demo.configserver;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.config.server.config.CompositeConfiguration;
import org.springframework.cloud.config.server.config.ConfigServerAutoConfiguration;
import org.springframework.cloud.config.server.config.ConfigServerConfiguration;
import org.springframework.cloud.config.server.config.ConfigServerEncryptionConfiguration;
import org.springframework.cloud.config.server.config.ConfigServerMvcConfiguration;
import org.springframework.cloud.config.server.config.ConfigServerProperties;
import org.springframework.cloud.config.server.config.ResourceRepositoryConfiguration;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Olga Maciaszek-Sharma
 */
@SpringBootTest
class ConfigServerApplicationTests {

	@Autowired
	ApplicationContext applicationContext;

	@Test
	void shouldLoadContext() {
		assertThat(applicationContext.getBean(ConfigServerConfiguration.class))
				.isNotNull();
		assertThat(applicationContext.getBean(CompositeConfiguration.class)).isNotNull();
		assertThat(applicationContext.getBean(ResourceRepositoryConfiguration.class))
				.isNotNull();
		assertThat(applicationContext.getBean(ConfigServerEncryptionConfiguration.class))
				.isNotNull();
		assertThat(applicationContext.getBean(ConfigServerMvcConfiguration.class))
				.isNotNull();
		assertThat(applicationContext.getBean(ConfigServerAutoConfiguration.class))
				.isNotNull();
		assertThat(applicationContext.getBean(ConfigServerProperties.class)).isNotNull();
	}

}