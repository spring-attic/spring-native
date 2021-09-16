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
 * @author Sebastien Deleuze
 * @see <a href="https://www.graalvm.org/reference-manual/native-image/Resources/">GraalVM native image resource documentation</a>
 */
public class NativeResourcesEntry {

	private final Kind kind;

	private final String value;

	private NativeResourcesEntry(Kind kind, String value) {
		this.kind = kind;
		this.value = value;
	}

	/**
	 * Create a new {@link NativeResourcesEntry} for the specified resource pattern.
	 * @param resource Java regexp that matches resource(s) to be included
	 * @return a resource entry
	 */
	public static NativeResourcesEntry of(String resource) {
		Assert.notNull(resource, "Pattern must not be null");
		return new NativeResourcesEntry(Kind.PATTERN, resource);
	}

	/**
	 * Create a new {@link NativeResourcesEntry} for the specified bundle pattern.
	 * @param bundle Java regexp that matches bundle(s) to be included
	 * @return a resource entry
	 */
	public static NativeResourcesEntry ofBundle(String bundle) {
		Assert.notNull(bundle, "bundle must not be null");
		return new NativeResourcesEntry(Kind.BUNDLE, bundle);
	}

	/**
	 * Create a new {@link NativeResourcesEntry} for the specified class name. This allows
	 * reading ASM metadata at runtime.
	 * @param className the type to consider
	 * @return a resource entry
	 */
	public static NativeResourcesEntry ofClassName(String className) {
		Assert.notNull(className, "ClassName must not be null");
		return new NativeResourcesEntry(Kind.CLASS, className);
	}

	/**
	 * Create a new {@link NativeResourcesEntry} for the specified class. This allows
	 * reading ASM metadata at runtime.
	 * @param type the type to consider
	 * @return a resource entry
	 */
	public static NativeResourcesEntry ofClass(Class<?> type) {
		Assert.notNull(type, "Type must not be null");
		return ofClassName(type.getName());
	}

	public void contribute(ResourcesDescriptor descriptor) {
		switch (this.kind) {
			case PATTERN:
				descriptor.add(this.value);
				break;
			case BUNDLE:
				descriptor.addBundle(this.value);
				break;
			case CLASS:
				descriptor.addClass(this.value);
				break;
		}
	}

	enum Kind {

		/** A resource pattern */
		PATTERN,
		
		/** A resource bundle pattern */
		BUNDLE,

		/** A class that requires access to its *.class resource. This allows reading ASM metadata at runtime. */
		CLASS
	}
}
