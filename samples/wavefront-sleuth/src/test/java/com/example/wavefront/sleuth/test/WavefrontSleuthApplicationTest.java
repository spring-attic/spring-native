package com.example.wavefront.sleuth.test;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.sleuth.autoconfig.TraceConfiguration;
import org.springframework.cloud.sleuth.autoconfig.brave.SleuthPropagationProperties;
import org.springframework.cloud.sleuth.autoconfig.brave.SleuthProperties;
import org.springframework.cloud.sleuth.autoconfig.brave.instrument.web.BraveHttpConfiguration;
import org.springframework.cloud.sleuth.autoconfig.instrument.messaging.SleuthMessagingProperties;
import org.springframework.cloud.sleuth.autoconfig.instrument.web.TraceWebAutoConfiguration;
import org.springframework.cloud.sleuth.autoconfig.instrument.web.client.TraceWebAsyncClientAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = "management.metrics.export.wavefront.enabled=false")
@SpringBootTest
public class WavefrontSleuthApplicationTest {

	@Autowired
	ApplicationContext applicationContext;

	@Test
	public void contextLoads() {
		assertThat(applicationContext.getBean(BraveHttpConfiguration.class)).isNotNull();
		assertThat(applicationContext.getBean(TraceConfiguration.class)).isNotNull();
		assertThat(applicationContext.getBean(SleuthPropagationProperties.class))
				.isNotNull();
		assertThat(applicationContext.getBean(SleuthProperties.class)).isNotNull();
		assertThat(applicationContext.getBean(SleuthMessagingProperties.class))
				.isNotNull();
		assertThat(applicationContext.getBean(TraceWebAsyncClientAutoConfiguration.class))
				.isNotNull();
		assertThat(applicationContext.getBean(TraceWebAutoConfiguration.class))
				.isNotNull();
	}

}
