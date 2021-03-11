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
import java.io.InputStream;

import org.springframework.asm.ClassReader;
import org.springframework.core.NestedIOException;
import org.springframework.core.io.Resource;

/**
 * Read the {@link ClassDescriptor} information from a class resource using ASM.
 * @author Brian Clozel
 */
class DefaultClassDescriptorReader {

	private static final int DESCRIPTOR_PARSING_OPTIONS = ClassReader.SKIP_DEBUG
			| ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES;

	static DefaultClassDescriptor readDescriptor(TypeSystem typeSystem, Resource resource) throws IOException {
		DefaultClassDescriptorVisitor visitor = new DefaultClassDescriptorVisitor(typeSystem);
		getClassReader(resource).accept(visitor, DESCRIPTOR_PARSING_OPTIONS);
		return visitor.getDescriptor();
	}

	private static ClassReader getClassReader(Resource resource) throws IOException {
		try (InputStream is = resource.getInputStream()) {
			try {
				return new ClassReader(is);
			}
			catch (IllegalArgumentException ex) {
				throw new NestedIOException("ASM ClassReader failed to parse class file - " +
						"probably due to a new Java class file version that isn't supported yet: " + resource, ex);
			}
		}
	}
}
