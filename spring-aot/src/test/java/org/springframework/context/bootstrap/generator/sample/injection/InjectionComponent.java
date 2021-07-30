package org.springframework.context.bootstrap.generator.sample.injection;

import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("unused")
public class InjectionComponent {

	private final String bean;

	private Integer counter;

	public InjectionComponent(String bean) {
		this.bean = bean;
	}

	@Autowired(required = false)
	public void setCounter(Integer counter) {
		this.counter = counter;
	}

}
