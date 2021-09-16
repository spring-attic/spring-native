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

package org.springframework.context.bootstrap.generator.infrastructure.nativex;

import org.junit.jupiter.api.Test;

import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeResourcesEntryTests.InnerClassSample.AnotherInner;
import org.springframework.nativex.domain.resources.ResourcesDescriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link NativeResourcesEntry}.
 *
 * @author Stephane Nicoll
 * @author Sebastien Deleuze
 */
class NativeResourcesEntryTests {

	@Test
	void ofWithNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> NativeResourcesEntry.of(null));
	}

	@Test
	void ofBundleWithNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> NativeResourcesEntry.ofBundle(null));
	}

	@Test
	void ofClassNameWithNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> NativeResourcesEntry.ofClassName(null));
	}

	@Test
	void contributeTopLevelClassName() {
		ResourcesDescriptor resourcesDescriptor = new ResourcesDescriptor();
		NativeResourcesEntry.ofClassName("java.lang.String").contribute(resourcesDescriptor);
		assertThat(resourcesDescriptor.getPatterns()).singleElement()
				.satisfies((pattern) -> assertThat(pattern).isEqualTo("java/lang/String.class"));
		assertThat(resourcesDescriptor.getBundles()).isEmpty();
	}

	@Test
	void contributeInnerClass() {
		ResourcesDescriptor resourcesDescriptor = new ResourcesDescriptor();
		NativeResourcesEntry.ofClass(AnotherInner.class).contribute(resourcesDescriptor);
		assertThat(resourcesDescriptor.getPatterns()).singleElement()
				.satisfies((pattern) -> assertThat(pattern).endsWith("/NativeResourcesEntryTests\\$InnerClassSample\\$AnotherInner.class"));
		assertThat(resourcesDescriptor.getBundles()).isEmpty();
	}

	@Test
	void contributeResource() {
		ResourcesDescriptor resourcesDescriptor = new ResourcesDescriptor();
		NativeResourcesEntry.of("foo/bar.txt").contribute(resourcesDescriptor);
		assertThat(resourcesDescriptor.getPatterns()).singleElement()
				.satisfies((pattern) -> assertThat(pattern).isEqualTo("foo/bar.txt"));
		assertThat(resourcesDescriptor.getBundles()).isEmpty();
	}

	@Test
	void contributeBundle() {
		ResourcesDescriptor resourcesDescriptor = new ResourcesDescriptor();
		NativeResourcesEntry.ofBundle("foo.bar").contribute(resourcesDescriptor);
		assertThat(resourcesDescriptor.getPatterns()).isEmpty();
		assertThat(resourcesDescriptor.getBundles()).singleElement()
				.satisfies((pattern) -> assertThat(pattern).isEqualTo("foo.bar"));;
	}

	static class InnerClassSample {

		static class AnotherInner {

		}
	}

}
