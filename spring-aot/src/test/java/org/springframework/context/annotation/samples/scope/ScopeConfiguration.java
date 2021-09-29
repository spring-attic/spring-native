package org.springframework.context.annotation.samples.scope;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.bootstrap.generator.sample.factory.NumberHolder;
import org.springframework.context.bootstrap.generator.sample.factory.StringHolder;

@Configuration(proxyBeanMethods = false)
public class ScopeConfiguration {

	private static final AtomicInteger counter = new AtomicInteger();

	@Bean
	@Scope("prototype")
	public NumberHolder<Integer> counterBean() {
		return new NumberHolder<>(counter.getAndIncrement());
	}

	@Bean
	@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StringHolder timeBean() {
		return new StringHolder(Instant.now().toString());
	}

}
