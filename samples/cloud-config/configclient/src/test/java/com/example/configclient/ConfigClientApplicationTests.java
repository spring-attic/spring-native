package com.example.configclient;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.config.client.ConfigClientAutoConfiguration;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Olga Maciaszek-Sharma
 */
@SpringBootTest
class ConfigClientApplicationTests {

	@Autowired
	ApplicationContext applicationContext;

	@Test
	void shouldLoadContext() {
		assertThat(applicationContext.getBean(ConfigClientAutoConfiguration.class))
				.isNotNull();
	}

}