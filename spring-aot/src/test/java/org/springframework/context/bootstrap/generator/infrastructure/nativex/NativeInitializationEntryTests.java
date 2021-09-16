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

import org.springframework.nativex.domain.init.InitializationDescriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link NativeInitializationEntry}.
 *
 * @author Sebastien Deleuze
 */
public class NativeInitializationEntryTests {

	@Test
	void ofRuntimeTypeWithNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> NativeInitializationEntry.ofRuntimeType(null));
	}

	@Test
	void contributeRuntimeType() {
		InitializationDescriptor initializationDescriptor = new InitializationDescriptor();
		NativeInitializationEntry.ofRuntimeType(String.class).contribute(initializationDescriptor);
		assertThat(initializationDescriptor.getRuntimeClasses()).singleElement().isEqualTo(String.class.getName());
		assertThat(initializationDescriptor.getBuildtimeClasses()).isEmpty();
		assertThat(initializationDescriptor.getRuntimePackages()).isEmpty();
		assertThat(initializationDescriptor.getBuildtimePackages()).isEmpty();
	}

	@Test
	void ofBuildTimeTypeWithNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> NativeInitializationEntry.ofBuildTimeType(null));
	}

	@Test
	void contributeBuildTimeType() {
		InitializationDescriptor initializationDescriptor = new InitializationDescriptor();
		NativeInitializationEntry.ofBuildTimeType(String.class).contribute(initializationDescriptor);
		assertThat(initializationDescriptor.getRuntimeClasses()).isEmpty();
		assertThat(initializationDescriptor.getBuildtimeClasses()).singleElement().isEqualTo(String.class.getName());
		assertThat(initializationDescriptor.getRuntimePackages()).isEmpty();
		assertThat(initializationDescriptor.getBuildtimePackages()).isEmpty();
	}

	@Test
	void ofRuntimePackageWithNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> NativeInitializationEntry.ofRuntimePackage(null));
	}

	@Test
	void contributeRuntimePackage() {
		InitializationDescriptor initializationDescriptor = new InitializationDescriptor();
		NativeInitializationEntry.ofRuntimePackage("foo.bar").contribute(initializationDescriptor);
		assertThat(initializationDescriptor.getRuntimeClasses()).isEmpty();
		assertThat(initializationDescriptor.getBuildtimeClasses()).isEmpty();
		assertThat(initializationDescriptor.getRuntimePackages()).singleElement().isEqualTo("foo.bar");
		assertThat(initializationDescriptor.getBuildtimePackages()).isEmpty();
	}

	@Test
	void ofBuildTimePackageWithNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> NativeInitializationEntry.ofBuildTimePackage(null));
	}

	@Test
	void contributeBuildTimePackage() {
		InitializationDescriptor initializationDescriptor = new InitializationDescriptor();
		NativeInitializationEntry.ofBuildTimePackage("foo.bar").contribute(initializationDescriptor);
		assertThat(initializationDescriptor.getRuntimeClasses()).isEmpty();
		assertThat(initializationDescriptor.getBuildtimeClasses()).isEmpty();
		assertThat(initializationDescriptor.getRuntimePackages()).isEmpty();
		assertThat(initializationDescriptor.getBuildtimePackages()).singleElement().isEqualTo("foo.bar");
	}

}
