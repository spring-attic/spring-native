/*
 * Copyright 2002-2021 the original author or authors.
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

package org.springframework.context.bootstrap.generator;

import java.util.List;

import com.squareup.javapoet.JavaFile;

import org.springframework.nativex.domain.reflect.ClassDescriptor;


/**
 * Provide the result of the the {@link ContextBootstrapGenerator} processing.
 *
 * @author Brian Clozel
 */
public class BootstrapGenerationResult {

	private final List<JavaFile> sourceFiles;

	private final List<ClassDescriptor> ClassDescriptor;

	BootstrapGenerationResult(List<JavaFile> sourceFiles, List<ClassDescriptor> classDescriptors) {
		this.sourceFiles = sourceFiles;
		this.ClassDescriptor = classDescriptors;
	}

	/**
	 * Return the {@link JavaFile source files}.
	 * @return the context bootstrap classes
	 */
	public List<JavaFile> getSourceFiles() {
		return this.sourceFiles;
	}

	/**
	 * Return the {@link ClassDescriptor reflection entries} that are necessary to process
	 * the bootstrap of the context.
	 * @return the reflection entries
	 */
	public List<ClassDescriptor> getClassDescriptors() {
		return this.ClassDescriptor;
	}

}
