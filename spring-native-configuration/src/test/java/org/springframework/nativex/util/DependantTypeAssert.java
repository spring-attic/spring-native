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
package org.springframework.nativex.util;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.springframework.nativex.type.AccessDescriptor;
import org.springframework.nativex.type.HintDeclaration;

public class DependantTypeAssert extends AbstractAssert<DependantTypeAssert, HintDeclaration> {

	private String typeName;

	public DependantTypeAssert(Class<?> type, HintDeclaration hintDeclaration) {
		this(type.getName(), hintDeclaration);
	}

	public DependantTypeAssert(String typeName, HintDeclaration hintDeclaration) {

		super(hintDeclaration, DependantTypeAssert.class);
		this.typeName = typeName;
	}

	public DependantTypeAssert hasAccessBits(int accessBits) {
		return hasAccessDescriptor(new AccessDescriptor(accessBits));
	}

	public DependantTypeAssert hasAccessDescriptor(AccessDescriptor accessDescriptor) {

		isNotNull();
		if (!actual.getDependantTypes().containsKey(typeName)) {
			failWithMessage("Expected to contain reflection config for %s but was not in %s",
					typeName, actual.getDependantTypes());
		}

		AccessDescriptor ad = actual.getDependantTypes().get(typeName);
		Assertions.assertThat(ad.toString()).isEqualTo(accessDescriptor.toString());

		return this;
	}
}
