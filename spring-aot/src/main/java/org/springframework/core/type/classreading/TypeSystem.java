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

package org.springframework.core.type.classreading;

import java.util.stream.Stream;

import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;

/**
 * @author Brian Clozel
 */
public interface TypeSystem {

	@Nullable
	TypeDescriptor resolve(String typeName);

	@Nullable
	default ClassDescriptor resolveClass(String className) {
		TypeDescriptor resolvedType = resolve(className);
		if (resolvedType != null) {
			return resolvedType.getClassDescriptor();
		}
		return null;
	}

	Stream<ClassDescriptor> scan(String basePackage);

	static TypeSystem getTypeSystem(ResourceLoader resourceLoader) {
		return new DefaultTypeSystem(resourceLoader);
	}

	ResourceLoader getResourceLoader();

}