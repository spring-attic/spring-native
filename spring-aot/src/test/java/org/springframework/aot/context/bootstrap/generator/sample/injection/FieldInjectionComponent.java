package org.springframework.aot.context.bootstrap.generator.sample.injection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

public class FieldInjectionComponent {

	@Autowired
	private Environment environment;

	@Autowired(required = false)
	private String bean;

}
