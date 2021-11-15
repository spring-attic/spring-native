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

package org.springframework.aot.context.bootstrap.generator.test;

import java.beans.Introspector;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;

import org.springframework.aot.context.bootstrap.generator.ApplicationContextAotProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.DefaultBootstrapWriterContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.ClassUtils;

/**
 * A tester for {@link ApplicationContextAotProcessor}.
 *
 * @author Stephane Nicoll
 */
public class ApplicationContextAotProcessorTester {

	public static final String CLASS_NAME = "ContextBootstrapInitializer";

	private final Path directory;

	private final ClassName className;

	public ApplicationContextAotProcessorTester(Path directory, ClassName className) {
		this.directory = directory;
		this.className = className;
	}

	public ApplicationContextAotProcessorTester(Path directory) {
		this(directory, ClassName.get("com.exemple", CLASS_NAME));
	}

	public ContextBootstrapStructure process(Class<?>... candidates) {
		GenericApplicationContext context = new AnnotationConfigApplicationContext();
		for (Class<?> candidate : candidates) {
			context.registerBean(generateShortName(candidate), candidate);
		}
		Path srcDirectory = generateSrcDirectory();
		DefaultBootstrapWriterContext writerContext = new DefaultBootstrapWriterContext(
				this.className.packageName(), this.className.simpleName());
		new ApplicationContextAotProcessor(context.getClassLoader()).process(context, writerContext);
		writeSources(srcDirectory, writerContext.toJavaFiles());
		return new ContextBootstrapStructure(srcDirectory, this.className, writerContext
				.getNativeConfigurationRegistry().reflection().toClassDescriptors());
	}

	private String generateShortName(Class<?> target) {
		String shortClassName = ClassUtils.getShortName(target);
		return Introspector.decapitalize(shortClassName);
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
