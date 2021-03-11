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

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * Default implementation for {@link TypeDescriptor}
 * @author Brian Clozel
 */
class DefaultTypeDescriptor implements TypeDescriptor {

	private final TypeName typeName;

	private final TypeSystem typeSystem;

	@Nullable
	private ClassDescriptor classDescriptor;

	DefaultTypeDescriptor(String typeName, TypeSystem typeSystem) throws IOException {
		//TODO: handle primitive types
		this.typeSystem = typeSystem;
		this.typeName = TypeName.from(typeName);
		if (!this.typeName.isPrimitive() && this.typeName.getConstructorName() != null) {
			Resource classResource = getClassResource(this.typeName.getClassName());
			if (classResource != null && classResource.exists()) {
				this.classDescriptor = DefaultClassDescriptorReader.readDescriptor(this.typeSystem, classResource);
			}
		}
	}

	@Nullable
	private Resource getClassResource(String className) {
		String resourcePath = ResourceLoader.CLASSPATH_URL_PREFIX +
				ClassUtils.convertClassNameToResourcePath(className) + ClassUtils.CLASS_FILE_SUFFIX;
		Resource classResource = this.typeSystem.getResourceLoader().getResource(resourcePath);
		if (classResource.exists()) {
			return classResource;
		}
		else {
			// Maybe an inner class name using the dot name syntax? Need to use the dollar syntax here...
			// ClassUtils.forName has an equivalent check for resolution into Class references later on.
			int lastDotIndex = className.lastIndexOf('.');
			if (lastDotIndex != -1) {
				String innerClassName =
						className.substring(0, lastDotIndex) + '$' + className.substring(lastDotIndex + 1);
				String innerClassResourcePath = ResourceLoader.CLASSPATH_URL_PREFIX +
						ClassUtils.convertClassNameToResourcePath(innerClassName) + ClassUtils.CLASS_FILE_SUFFIX;
				Resource innerClassResource = this.typeSystem.getResourceLoader().getResource(innerClassResourcePath);
				if (innerClassResource.exists()) {
					return innerClassResource;
				}
			}
		}
		return null;
	}

	@Override
	public String getTypeName() {
		return this.typeName.getClassName();
	}

	@Override
	public int getArrayDimensions() {
		return this.typeName.getArrayDimensions();
	}

	@Override
	public boolean isPrimitiveType() {
		return this.typeName.isPrimitive();
	}

	@Override
	public ClassDescriptor getClassDescriptor() {
		return this.classDescriptor;
	}

	@Override
	public TypeSystem getTypeSystem() {
		return this.typeSystem;
	}
}