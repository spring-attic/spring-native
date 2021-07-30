package org.springframework.context.bootstrap.generator.sample.injection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration(proxyBeanMethods = false)
@SuppressWarnings("unused")
public class InjectionConfiguration {

	@Bean
	InjectionComponent injectionComponent() {
		return new InjectionComponent("test");
	}

	@Autowired
	public void setEnvironment(Environment environment) {

	}

	@Autowired(required = false)
	public void setBean(String bean) {

	}

}
