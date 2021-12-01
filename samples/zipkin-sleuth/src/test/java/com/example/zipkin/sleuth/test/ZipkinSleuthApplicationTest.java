package com.example.zipkin.sleuth.test;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.sleuth.autoconfig.SleuthAnnotationConfiguration;
import org.springframework.cloud.sleuth.autoconfig.TraceConfiguration;
import org.springframework.cloud.sleuth.autoconfig.brave.BraveAutoConfiguration;
import org.springframework.cloud.sleuth.autoconfig.brave.SleuthProperties;
import org.springframework.cloud.sleuth.autoconfig.zipkin2.ZipkinAutoConfiguration;
import org.springframework.cloud.sleuth.zipkin2.ZipkinProperties;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ZipkinSleuthApplicationTest {

	@Autowired
	ApplicationContext applicationContext;

	@Test
	public void contextLoads() {
		assertThat(applicationContext.getBeansOfType(ZipkinAutoConfiguration.class))
				.isNotNull();
		assertThat(applicationContext.getBeansOfType(ZipkinProperties.class)).isNotNull();
		assertThat(applicationContext.getBeansOfType(TraceConfiguration.class))
				.isNotNull();
		assertThat(applicationContext.getBeansOfType(SleuthAnnotationConfiguration.class))
				.isNotNull();
		assertThat(applicationContext.getBeansOfType(SleuthProperties.class)).isNotNull();
		assertThat(applicationContext.getBeansOfType(BraveAutoConfiguration.class))
				.isNotNull();
	}

}
