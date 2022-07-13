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

import org.springframework.nativex.domain.serialization.SerializationDescriptor;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Describe the need for serialization configuration.
 *
 * @author Sebastien Deleuze
 */
public class NativeSerializationEntry {

	private final Class<?> type;
	private final boolean lambdaCapturing;

	private NativeSerializationEntry(Class<?> type) {
		this(type, false);
	}

	private NativeSerializationEntry(Class<?> type, boolean lambdaCapturing) {
		this.type = type;
		this.lambdaCapturing = lambdaCapturing;
	}

	/**
	 * Create a new {@link NativeSerializationEntry} for the specified types.
	 * @param type the related type
	 * @return the serialization entry
	 */
	public static NativeSerializationEntry ofType(Class<?> type) {
		Assert.notNull(type, "type must not be null");
		return new NativeSerializationEntry(type);
	}

	/**
	 * Create a new {@link NativeSerializationEntry} for the lambda capturing types.
	 * @param type the lambda capturing type
	 * @return the serialization entry
	 */
	public static NativeSerializationEntry ofLambdaCapturingType(Class<?> type) {
		Assert.notNull(type, "type must not be null");
		return new NativeSerializationEntry(type, true);
	}

	/**
	 * Create a new {@link NativeSerializationEntry} for the specified types.
	 * @param typeName the related type name
	 * @return the serialization entry
	 */
	public static NativeSerializationEntry ofTypeName(String typeName) {
		Assert.notNull(typeName, "typeName must not be null");
		return new NativeSerializationEntry(ClassUtils.resolveClassName(typeName, null));
	}

	/**
	 * Create a new {@link NativeSerializationEntry} for the lambda capturing types.
	 * @param typeName the lambda capturing type name
	 * @return the serialization entry
	 */
	public static NativeSerializationEntry ofLambdaCapturingTypeName(String typeName) {
		Assert.notNull(typeName, "typeName must not be null");
		return new NativeSerializationEntry(ClassUtils.resolveClassName(typeName, null), true);
	}

	public void contribute(SerializationDescriptor descriptor) {
		descriptor.add(this.type.getName(), lambdaCapturing);
	}
}
