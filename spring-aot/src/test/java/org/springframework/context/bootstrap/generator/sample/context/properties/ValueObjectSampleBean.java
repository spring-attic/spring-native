package org.springframework.context.bootstrap.generator.sample.context.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties("test")
@ConstructorBinding
public class ValueObjectSampleBean {

	private final String name;

	public ValueObjectSampleBean(String name) {
		this.name = name;
	}

}
