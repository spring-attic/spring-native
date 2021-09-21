/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.bootstrap.generator.bean.descriptor;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.MemberDescriptor;
import org.springframework.context.bootstrap.generator.sample.callback.ConfigHavingBeansWithInitMethod.HavingPublicExternallyManagedInitMethodInParent;
import org.springframework.context.bootstrap.generator.sample.callback.ConfigHavingBeansWithInitMethod.NoInitMethod;
import org.springframework.context.bootstrap.generator.sample.callback.ConfigHavingBeansWithInitMethod.PrivateExternallyManagedInitMethod;
import org.springframework.context.bootstrap.generator.sample.callback.ConfigHavingBeansWithInitMethod.PublicExternallyManagedInitMethod;

/**
 * Tests for {@link InitializationMethodSupplier}.
 *
 * @author Christoph Strobl
 */
class InitializationMethodSupplierTests {

	private final InitializationMethodSupplier supplier = new InitializationMethodSupplier();
	private final CommonAnnotationBeanPostProcessor postProcessor = new CommonAnnotationBeanPostProcessor();

	@Test // GH-1048
	void emptyCallbacksForTypesThatDoNotDefineAnInitMethod() {

		List<MemberDescriptor<Method>> callbacks = computeCallbacks(NoInitMethod.class);
		assertThat(callbacks).isEmpty();
	}

	@Test // GH-1048
	void publicInitMethod() {

		List<MemberDescriptor<Method>> callbacks = computeCallbacks(PublicExternallyManagedInitMethod.class);

		assertThat(callbacks).singleElement().satisfies((callback) -> assertThat(callback.getMember().getName()).isEqualTo("publicInit"));
	}

	@Test // GH-1048
	void privateInitMethod() {

		List<MemberDescriptor<Method>> callbacks = computeCallbacks(PrivateExternallyManagedInitMethod.class);

		assertThat(callbacks).singleElement().satisfies((callback) -> assertThat(callback.getMember().getName()).isEqualTo("privateInit"));
	}

	@Test // GH-1048
	void inheritedInitMethod() {

		List<MemberDescriptor<Method>> callbacks = computeCallbacks(HavingPublicExternallyManagedInitMethodInParent.class);

		assertThat(callbacks).singleElement().satisfies((callback) -> assertThat(callback.getMember().getName()).isEqualTo("publicInit"));
	}

	private List<MemberDescriptor<Method>> computeCallbacks(Class<?> beanTargetType) {

		RootBeanDefinition beanDefinition = new RootBeanDefinition(beanTargetType);
		postProcessor.postProcessMergedBeanDefinition(beanDefinition, beanDefinition.getResolvableType().toClass(), "targetBeanName");

		return supplier.getInstanceCallbacks(beanDefinition);
	}
}
