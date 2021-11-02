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

package org.springframework.aot.test.context.bootstrap.generator.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import com.squareup.javapoet.JavaFile;

import org.springframework.aot.context.bootstrap.generator.infrastructure.BootstrapWriterContext;
import org.springframework.aot.context.bootstrap.generator.infrastructure.DefaultBootstrapWriterContext;
import org.springframework.aot.test.context.bootstrap.generator.TestContextAotProcessor;

/**
 * A tester for {@link TestContextAotProcessor}.
 *
 * @author Stephane Nicoll
 */
public class TestContextAotProcessorTester {

	private final Path directory;

	private final String packageName;

	public TestContextAotProcessorTester(Path directory, String packageName) {
		this.directory = directory;
		this.packageName = packageName;
	}

	public TestContextAotProcessorTester(Path directory) {
		this(directory, "com.example");
	}

	public ContextBootstrapStructure process(Class<?>... testClasses) {
		Path srcDirectory = generateSrcDirectory();
		DefaultBootstrapWriterContext writerContext = new DefaultBootstrapWriterContext(this.packageName,
				BootstrapWriterContext.bootstrapClassFactory(this.packageName, "Test"));
		new TestContextAotProcessor(getClass().getClassLoader())
				.generateTestContexts(Arrays.asList(testClasses), writerContext);
		writeSources(srcDirectory, writerContext.toJavaFiles());
		return new ContextBootstrapStructure(srcDirectory, this.packageName, writerContext.getNativeConfigurationRegistry());
	}

	private Path generateSrcDirectory() {
		try {
			return Files.createTempDirectory(this.directory, "bootstrap-");
		}
		catch (IOException ex) {
			throw new IllegalStateException("Failed to create generate source structure ", ex);
		}
	}

	private void writeSources(Path srcDirectory, List<JavaFile> javaFiles) {
		try {
			for (JavaFile javaFile : javaFiles) {
				javaFile.writeTo(srcDirectory);
			}
		}
		catch (IOException ex) {
			throw new IllegalStateException("Failed to write source code to disk ", ex);
		}
	}

}
