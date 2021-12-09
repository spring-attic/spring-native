package com.example.configclient;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.cloud.config.client.ConfigClientAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Olga Maciaszek-Sharma
 */
@SpringBootTest
class ConfigClientApplicationTests {

	@Autowired
	ConfigurableApplicationContext applicationContext;

	@Test
	void shouldLoadContext() {
		AssertableApplicationContext context = AssertableApplicationContext
				.get(() -> applicationContext);
		assertThat(context).hasSingleBean(ConfigClientAutoConfiguration.class);
	}

}