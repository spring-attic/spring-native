package org.springframework.context.bootstrap.generator.sample.visibility;

import org.springframework.core.ResolvableType;

public class PublicFactoryBean<T> {

	public static ResolvableType resolveToProtectedGenericParameter() {
		return ResolvableType.forClassWithGenerics(PublicFactoryBean.class, ProtectedType.class);
	}

}
