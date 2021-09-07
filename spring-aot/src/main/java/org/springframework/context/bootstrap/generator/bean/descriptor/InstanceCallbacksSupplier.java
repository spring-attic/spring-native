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

import java.util.ArrayList;
import java.util.List;

import com.squareup.javapoet.CodeBlock;

import org.springframework.aot.context.annotation.ImportAwareInvoker;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.InstanceCallback;

/**
 * Provide the {@link InstanceCallback instance callbacks} of a given class. These are
 * only callbacks that are required for {@code @Configuration} processing for now.
 *
 * @author Stephane Nicoll
 */
class InstanceCallbacksSupplier {

	List<InstanceCallback> detectInstanceCallbacks(Class<?> type) {
		List<InstanceCallback> callbacks = new ArrayList<>();
		if (ImportAware.class.isAssignableFrom(type)) {
			callbacks.add(getImportAwareCallback());
		}
		return callbacks;
	}

	private InstanceCallback getImportAwareCallback() {
		return new InstanceCallback((variable) -> CodeBlock.of(
				"$T.get(context).setAnnotationMetadata($L)", ImportAwareInvoker.class, variable));
	}

}
