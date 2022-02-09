/*
 * Copyright 2019-2022 the original author or authors.
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

package org.springframework.aot.test.build;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.MultiValueMap;

/**
 * Find Spring test Classes, which means classes annotated or meta-annotated with
 * {@code @ExtendWith(SpringExtension.class)}.
 *
 * @author Brian Clozel
 */
public abstract class TestClassesFinder {

	public static final String EXTEND_WITH_ANNOTATION_NAME = "org.junit.jupiter.api.extension.ExtendWith";

	public static final String SPRING_EXTENSION_ANNOTATION_NAME = "org.springframework.test.context.junit.jupiter.SpringExtension";

	private static final FileFilter CLASS_FILE_FILTER = TestClassesFinder::isClassFile;

	private static final FileFilter PACKAGE_DIRECTORY_FILTER = TestClassesFinder::isPackageDirectory;


	public static List<String> findTestClasses(Path rootDirectoryPath) throws IOException {
		List<String> testClasses = new ArrayList<>();
		SimpleMetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory();
		File rootDirectory = rootDirectoryPath.toFile();
		if (!rootDirectory.exists()) {
			return Collections.emptyList();
		}
		else if (!rootDirectory.isDirectory()) {
			throw new IllegalArgumentException("Invalid root directory '" + rootDirectory + "'");
		}
		else {
			Deque<File> stack = new ArrayDeque<>();
			stack.push(rootDirectory);
			while (!stack.isEmpty()) {
				File file = stack.pop();
				if (file.isFile()) {
					try {
						MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(new FileSystemResource(file));
						AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
						ClassMetadata classMetadata = metadataReader.getClassMetadata();
						MultiValueMap<String, Object> extendWith = annotationMetadata.getAllAnnotationAttributes(EXTEND_WITH_ANNOTATION_NAME, true);
						if (extendWith != null) {
							List<Object> extendWithValues = extendWith.getOrDefault("value", Collections.emptyList());
							boolean isSpringTest = extendWithValues.stream()
									.flatMap(classNames -> Arrays.stream((String[]) classNames))
									.anyMatch(className -> className.equals(SPRING_EXTENSION_ANNOTATION_NAME));
							if (isSpringTest) {
								testClasses.add(classMetadata.getClassName());
							}
						}
					}
					catch (IOException ex) {
						// ignore file as Test class candidate
					}
				}
				if (file.isDirectory()) {
					Arrays.stream(file.listFiles(PACKAGE_DIRECTORY_FILTER)).forEach(stack::push);
					Arrays.stream(file.listFiles(CLASS_FILE_FILTER)).forEach(stack::push);
				}

			}
		}
		return Collections.unmodifiableList(testClasses);
	}

	private static boolean isClassFile(File file) {
		return file.isFile() && file.getName().endsWith(".class");
	}

	private static boolean isPackageDirectory(File file) {
		return file.isDirectory() && !file.getName().startsWith(".");
	}

}
