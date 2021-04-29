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

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.assertj.core.api.AssertFactory;
import org.assertj.core.api.FactoryBasedNavigableListAssert;
import org.springframework.nativex.type.HintDeclaration;

public class HintDeclarationsAssert extends FactoryBasedNavigableListAssert<HintDeclarationsAssert, List<? extends HintDeclaration>, HintDeclaration, HintDeclarationAssert> {

	public HintDeclarationsAssert(List<? extends HintDeclaration> actual) {
		this(actual, HintDeclarationsAssert.class);
	}

	protected HintDeclarationsAssert(List<? extends HintDeclaration> hintDeclarations, Class<?> selfType) {
		super(hintDeclarations, HintDeclarationsAssert.class, new HintDeclarationsAssertFactory());
	}

	public static HintDeclarationsAssert assertHints(List<? extends HintDeclaration> hintDeclarations) {
		return new HintDeclarationsAssert(hintDeclarations);
	}

	public DependantTypeAssert extractReflectionConfigFor(Class<?> type) {
		return extractReflectionConfigFor(type.getName());
	}

	public HintDeclarationsAssert extractReflectionConfigFor(Class<?> type, Consumer<DependantTypeAssert> typeAssertConsumer) {

		typeAssertConsumer.accept(extractReflectionConfigFor(type));
		return this;
	}

	public DependantTypeAssert extractReflectionConfigFor(String type) {

		Optional<? extends HintDeclaration> first = actual.stream().filter(it -> {
			return it.getDependantTypes().containsKey(type);
		}).findFirst();

		if (!first.isPresent()) {
			failWithMessage("Expected to find at least one HintDeclaration to contain reflection config for %s but was not in %s",
					type, actual);
		}

		return new DependantTypeAssert(type, first.get());
	}

	public HintDeclarationsAssert doesNotContainResourceConfiguration() {

		for (HintDeclaration hint : actual) {
			new HintDeclarationAssert(hint).doesNotContainResources();
		}
		return this;
	}

	static public class HintDeclarationsAssertFactory implements AssertFactory<HintDeclaration, HintDeclarationAssert> {

		@Override
		public HintDeclarationAssert createAssert(HintDeclaration t) {
			return new HintDeclarationAssert(t);
		}
	}
}
