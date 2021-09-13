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

import org.springframework.nativex.domain.resources.ResourcesDescriptor;
import org.springframework.util.Assert;

/**
 * Collect the need for resources at runtime.
 *
 * @author Stephane Nicoll
 */
public class NativeResourcesEntry {

	private final String className;

	private NativeResourcesEntry(String className) {
		this.className = className;
	}

	/**
	 * Create a new {@link NativeResourcesEntry} for the specified class name. This allows
	 * reading ASM metadata at runtime.
	 * @param className the type to consider
	 * @return a resource entry
	 */
	public static NativeResourcesEntry ofClassName(String className) {
		Assert.notNull(className, "ClassName must not be null");
		return new NativeResourcesEntry(className);
	}

	/**
	 * Create a new {@link NativeResourcesEntry} for the specified class. This allows
	 * reading ASM metadata at runtime.
	 * @param type the type to consider
	 * @return a resource entry
	 */
	public static NativeResourcesEntry of(Class<?> type) {
		Assert.notNull(type, "Type must not be null");
		return ofClassName(type.getName());
	}

	public void contribute(ResourcesDescriptor descriptor) {
		descriptor.addClass(this.className);
	}

}
