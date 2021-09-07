/*
 * Copyright 2019-2021 the original author or authors.
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

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.InstanceCallback;
import org.springframework.context.bootstrap.generator.sample.callback.ImportAwareConfiguration;
import org.springframework.context.bootstrap.generator.test.CodeSnippet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link InstanceCallbacksSupplier}.
 *
 * @author Stephane Nicoll
 */
class InstanceCallbacksSupplierTests {

	private final InstanceCallbacksSupplier supplier = new InstanceCallbacksSupplier();

	@Test
	void detectInstanceCallbacksOnBeanDefinitionWithNoCallback() {
		List<InstanceCallback> callbacks = supplier.detectInstanceCallbacks(String.class);
		assertThat(callbacks).isEmpty();
	}

	@Test
	void detectInstanceCallbacksOnImportAwareImplementation() {
		List<InstanceCallback> callbacks = supplier.detectInstanceCallbacks(ImportAwareConfiguration.class);
		assertThat(callbacks).singleElement().satisfies((callback) -> assertThat(CodeSnippet.of(callback.write("beanRef")))
				.isEqualTo("ImportAwareInvoker.get(context).setAnnotationMetadata(beanRef)"));
	}

}
