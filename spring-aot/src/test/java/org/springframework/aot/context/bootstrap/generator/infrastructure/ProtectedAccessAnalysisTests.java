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

package org.springframework.aot.context.bootstrap.generator.infrastructure;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.infrastructure.ProtectedAccessAnalysis.ProtectedElement;
import org.springframework.aot.context.bootstrap.generator.sample.visibility.ProtectedConstructorComponent;
import org.springframework.aot.context.bootstrap.generator.sample.visibility.ProtectedFactoryMethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Tests for {@link ProtectedAccessAnalysis}.
 *
 * @author Stephane Nicoll
 */
class ProtectedAccessAnalysisTests {

	@Test
	void isAccessibleWithPublicElements() {
		ProtectedAccessAnalysis analysis = new ProtectedAccessAnalysis(Collections.emptyList());
		assertThat(analysis.isAccessible()).isTrue();
	}

	@Test
	void isAccessibleWithProtectedElement() {
		ProtectedAccessAnalysis analysis = new ProtectedAccessAnalysis(Collections.singletonList(
				ProtectedElement.of(ProtectedConstructorComponent.class, null)));
		assertThat(analysis.isAccessible()).isFalse();
	}

	@Test
	void getPrivilegedPackageNameWithPublicElements() {
		ProtectedAccessAnalysis analysis = new ProtectedAccessAnalysis(Collections.emptyList());
		assertThat(analysis.getPrivilegedPackageName()).isNull();
	}

	@Test
	void getPrivilegedPackageNameWithProtectedElement() {
		ProtectedAccessAnalysis analysis = new ProtectedAccessAnalysis(Collections.singletonList(
				ProtectedElement.of(ProtectedConstructorComponent.class, null)));
		assertThat(analysis.getPrivilegedPackageName()).isEqualTo(ProtectedConstructorComponent.class.getPackageName());
	}

	@Test
	void getPrivilegedPackageNameWithProtectedElementsFromSamePackage() {
		ProtectedAccessAnalysis analysis = new ProtectedAccessAnalysis(Arrays.asList(
				ProtectedElement.of(ProtectedConstructorComponent.class, null),
				ProtectedElement.of(ProtectedFactoryMethod.class, null)));
		assertThat(analysis.getPrivilegedPackageName()).isEqualTo(ProtectedConstructorComponent.class.getPackageName());
	}

	@Test
	void getPrivilegedPackageNameWithProtectedElementsFromDifferentPackages() {
		ProtectedAccessAnalysis analysis = new ProtectedAccessAnalysis(Arrays.asList(
				ProtectedElement.of(ProtectedConstructorComponent.class, null),
				ProtectedElement.of(String.class, null)));
		assertThatIllegalStateException().isThrownBy(analysis::getPrivilegedPackageName)
				.withMessageContaining(ProtectedConstructorComponent.class.getPackageName())
				.withMessageContaining(String.class.getPackageName());
	}

}
