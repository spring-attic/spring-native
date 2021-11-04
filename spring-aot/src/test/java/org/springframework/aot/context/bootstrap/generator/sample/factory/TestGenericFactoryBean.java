package org.springframework.aot.context.bootstrap.generator.sample.factory;

import java.io.Serializable;

import org.springframework.beans.factory.config.AbstractFactoryBean;

public class TestGenericFactoryBean<T extends Serializable> extends AbstractFactoryBean<T> {

	private final T instance;

	public TestGenericFactoryBean(T instance) {
		this.instance = instance;
	}

	@Override
	public Class<?> getObjectType() {
		return this.instance.getClass();
	}

	@Override
	protected T createInstance() {
		return this.instance;
	}

}
