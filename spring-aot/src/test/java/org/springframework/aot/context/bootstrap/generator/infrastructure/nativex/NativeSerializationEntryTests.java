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
package org.springframework.aot.context.bootstrap.generator.infrastructure.nativex;

import org.junit.jupiter.api.Test;

import org.springframework.nativex.domain.serialization.SerializationDescriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link NativeSerializationEntry}.
 *
 * @author Sebastien Deleuze
 */
public class NativeSerializationEntryTests {

	@Test
	void ofTypeWithNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> NativeSerializationEntry.ofType(null));
		assertThatIllegalArgumentException().isThrownBy(() -> NativeSerializationEntry.ofLambdaCapturingType(null));
	}

	@Test
	void contributeType() {
		SerializationDescriptor serializationDescriptor = new SerializationDescriptor();
		NativeSerializationEntry.ofType(String.class).contribute(serializationDescriptor);
		NativeSerializationEntry.ofLambdaCapturingType(String.class).contribute(serializationDescriptor);
		assertThat(serializationDescriptor.getSerializableTypes()).singleElement().isEqualTo(String.class.getName());
		assertThat(serializationDescriptor.getSerializableLambdaCapturingTypes()).singleElement().isEqualTo(String.class.getName());
	}

	@Test
	void ofTypeNameWithNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> NativeSerializationEntry.ofTypeName(null));
		assertThatIllegalArgumentException().isThrownBy(() -> NativeSerializationEntry.ofLambdaCapturingTypeName(null));
	}

	@Test
	void contributeTypeName() {
		SerializationDescriptor serializationDescriptor = new SerializationDescriptor();
		NativeSerializationEntry.ofTypeName(String.class.getName()).contribute(serializationDescriptor);
		NativeSerializationEntry.ofLambdaCapturingTypeName(String.class.getName()).contribute(serializationDescriptor);
		assertThat(serializationDescriptor.getSerializableTypes()).singleElement().isEqualTo(String.class.getName());
		assertThat(serializationDescriptor.getSerializableLambdaCapturingTypes()).singleElement().isEqualTo(String.class.getName());
	}
}
