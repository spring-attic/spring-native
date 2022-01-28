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

package org.springframework.aot.test.build;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Find Spring test Classes, which means classes annotated or meta-annotated with
 * {@code @ExtendWith(SpringExtension.class)}.
 *
 * @author Brian Clozel
 */
public abstract class TestClassesFinder {

	public static final String EXTEND_WITH_ANNOTATION_NAME = "org.junit.jupiter.api.extension.ExtendWith";

	public static final String SPRING_EXTENSION_ANNOTATION_NAME = "org.springframework.test.context.junit.jupiter.SpringExtension";

	public static final String NESTED_ANNOTATION_NAME = "org.junit.jupiter.api.Nested";

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
			Map<String, String> nestedClass=new HashMap<>();
			Deque<File> stack = new ArrayDeque<>();
			stack.push(rootDirectory);
			while (!stack.isEmpty()) {
				File file = stack.pop();
				if (file.isFile()) {
					try {
						MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(new FileSystemResource(file));
						AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
						ClassMetadata classMetadata = metadataReader.getClassMetadata();

						// Keeping track of nested classes as enclosing class may not be processed yet.
						if (annotationMetadata.hasAnnotation(NESTED_ANNOTATION_NAME) && classMetadata.hasEnclosingClass()){
							nestedClass.put(classMetadata.getClassName(), classMetadata.getEnclosingClassName());
							continue;
						}

						MultiValueMap<String, Object> extendWith=annotationMetadata.getAllAnnotationAttributes(EXTEND_WITH_ANNOTATION_NAME, true);
						if (extendWith != null) {
							List<Object> values=extendWith.getOrDefault("value", Collections.emptyList());
							boolean isStringTest = values.stream()
									.flatMap(x->Arrays.stream((String[])x))
									.anyMatch(x->x.equals(SPRING_EXTENSION_ANNOTATION_NAME));
							if (isStringTest) {
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

			// Processing nested class.
			// Iterating to check if enclosing class is a Spring test.
			while(!nestedClass.isEmpty()){
				Iterator<Map.Entry<String, String>> iterator = nestedClass.entrySet().iterator();

				while (iterator.hasNext()) {
					Map.Entry<String, String> entry = iterator.next();
					String enclosingClass = entry.getValue();
					String className = entry.getKey();

					if (testClasses.contains(enclosingClass)) {
						// Enclosing class is a Spring test, ok, this nested class is also a Spring test.
						testClasses.add(className);
						iterator.remove();
					}else if (!nestedClass.containsKey(enclosingClass)){
						// Enclosing class is not a Spring test, nor a nested class, it will never be a Spring test.
						iterator.remove();
					}
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
