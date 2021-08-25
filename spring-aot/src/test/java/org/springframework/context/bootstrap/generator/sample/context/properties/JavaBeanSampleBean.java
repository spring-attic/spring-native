package org.springframework.context.bootstrap.generator.sample.context.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("test")
public class JavaBeanSampleBean {

	private String name;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
