package com.example.zipkin.sleuth.test;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.cloud.sleuth.autoconfig.SleuthAnnotationConfiguration;
import org.springframework.cloud.sleuth.autoconfig.TraceConfiguration;
import org.springframework.cloud.sleuth.autoconfig.brave.BraveAutoConfiguration;
import org.springframework.cloud.sleuth.autoconfig.brave.SleuthProperties;
import org.springframework.cloud.sleuth.autoconfig.zipkin2.ZipkinAutoConfiguration;
import org.springframework.cloud.sleuth.zipkin2.ZipkinProperties;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ZipkinSleuthApplicationTest {

	@Autowired
	ConfigurableApplicationContext applicationContext;

	@Test
	public void contextLoads() {
		AssertableApplicationContext context = AssertableApplicationContext
				.get(() -> applicationContext);
		assertThat(context).hasSingleBean(ZipkinAutoConfiguration.class);
		assertThat(context).hasSingleBean(ZipkinProperties.class);
		assertThat(context).hasSingleBean(TraceConfiguration.class);
		assertThat(context).hasSingleBean(SleuthAnnotationConfiguration.class);
		assertThat(context).hasSingleBean(SleuthProperties.class);
		assertThat(context).hasSingleBean(BraveAutoConfiguration.class);
	}

}
