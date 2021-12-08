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

package org.springframework.aot.build;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.SpringAsmInfo;
import org.springframework.asm.Type;

/**
 * Finds any class annotated with a specified annotation.
 * Derived from Spring Boot {@code AnnotatedClassFinder}
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @author Sebastien Deleuze
 */
public abstract class AnnotatedClassFinder {

	private static final String DOT_CLASS = ".class";

	private static final FileFilter CLASS_FILE_FILTER = AnnotatedClassFinder::isClassFile;

	private static final FileFilter PACKAGE_DIRECTORY_FILTER = AnnotatedClassFinder::isPackageDirectory;

	private static boolean isClassFile(File file) {
		return file.isFile() && file.getName().endsWith(DOT_CLASS);
	}

	private static boolean isPackageDirectory(File file) {
		return file.isDirectory() && !file.getName().startsWith(".");
	}

	/**
	 * Find the class from a given directory.
	 * @param rootDirectory the root directory to search
	 * @param annotationName the annotation name
	 * @return the annotated class or {@code null}
	 * @throws IOException if the directory cannot be read
	 */
	public static String findAnnotatedClass(File rootDirectory, String annotationName) throws IOException {
		return doWithAnnotatedClasses(rootDirectory, annotationName, ApplicationClass::getName);
	}

	/**
	 * Find a single annotated class from the given {@code rootDirectory}. A annotated class
	 * annotated with an annotation with the given {@code annotationName} will be
	 * preferred over a annotated class with no such annotation.
	 * @param rootDirectory the root directory to search
	 * @param annotationName the name of the annotation that may be present on the main
	 * class
	 * @return the annotated class or {@code null}
	 * @throws IOException if the directory cannot be read
	 */
	public static String findSingleAnnotatedClass(File rootDirectory, String annotationName) throws IOException {
		AnnotatedClassFinder.SingleAnnotatedClassCallback callback = new AnnotatedClassFinder.SingleAnnotatedClassCallback();
		AnnotatedClassFinder.doWithAnnotatedClasses(rootDirectory, annotationName, callback);
		return callback.getAnnotatedClassName();
	}

	/**
	 * Perform the given callback operation on all annotated classes from the given root
	 * directory.
	 * @param <T> the result type
	 * @param rootDirectory the root directory
	 * @param callback the callback
	 * @return the first callback result or {@code null}
	 * @throws IOException in case of I/O errors
	 */
	static <T> T doWithAnnotatedClasses(File rootDirectory, String annotationName, AnnotatedClassFinder.AnnotatedClassCallback<T> callback) throws IOException {
		if (!rootDirectory.exists()) {
			return null; // nothing to do
		}
		if (!rootDirectory.isDirectory()) {
			throw new IllegalArgumentException("Invalid root directory '" + rootDirectory + "'");
		}
		String prefix = rootDirectory.getAbsolutePath() + "/";
		Deque<File> stack = new ArrayDeque<>();
		stack.push(rootDirectory);
		while (!stack.isEmpty()) {
			File file = stack.pop();
			if (file.isFile()) {
				try (InputStream inputStream = new FileInputStream(file)) {
					AnnotatedClassFinder.ClassDescriptor classDescriptor = createClassDescriptor(inputStream, annotationName);
					if (classDescriptor != null && classDescriptor.isAnnotationFound()) {
						String className = convertToClassName(file.getAbsolutePath(), prefix);
						T result = callback.doWith(new ApplicationClass(className));
						if (result != null) {
							return result;
						}
					}
				}
			}
			if (file.isDirectory()) {
				pushAllSorted(stack, file.listFiles(PACKAGE_DIRECTORY_FILTER));
				pushAllSorted(stack, file.listFiles(CLASS_FILE_FILTER));
			}
		}
		return null;
	}

	private static void pushAllSorted(Deque<File> stack, File[] files) {
		Arrays.sort(files, Comparator.comparing(File::getName));
		for (File file : files) {
			stack.push(file);
		}
	}


	private static String convertToClassName(String name, String prefix) {
		name = name.replace('/', '.');
		name = name.replace('\\', '.');
		name = name.substring(0, name.length() - DOT_CLASS.length());
		if (prefix != null) {
			name = name.substring(prefix.length());
		}
		return name;
	}

	private static AnnotatedClassFinder.ClassDescriptor createClassDescriptor(InputStream inputStream, String annotationName) {
		try {
			ClassReader classReader = new ClassReader(inputStream);
			AnnotatedClassFinder.ClassDescriptor classDescriptor = new AnnotatedClassFinder.ClassDescriptor(annotationName);
			classReader.accept(classDescriptor, ClassReader.SKIP_CODE);
			return classDescriptor;
		}
		catch (IOException ex) {
			return null;
		}
	}

	private static class ClassDescriptor extends ClassVisitor {

		private final String annotationName;

		private boolean annotationFound;

		ClassDescriptor(String annotationName) {
			super(SpringAsmInfo.ASM_VERSION);
			this.annotationName = annotationName;
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			if (annotationName.equals(Type.getType(desc).getClassName())) {
				annotationFound = true;
			}
			return null;
		}

		boolean isAnnotationFound() {
			return this.annotationFound;
		}
	}

	/**
	 * Callback for handling {@link ApplicationClass AnnotatedClasses}.
	 *
	 * @param <T> the callback's return type
	 */
	interface AnnotatedClassCallback<T> {

		/**
		 * Handle the specified annotated class.
		 * @param applicationClass the annotated class
		 * @return a non-null value if processing should end or {@code null} to continue
		 */
		T doWith(ApplicationClass applicationClass);

	}

	/**
	 * An application class.
	 */
	static final class ApplicationClass {

		private final String name;

		/**
		 * Creates a new {@code AnnotatedClass} rather represents the annotated class with the given
		 * {@code name}. The class is annotated with the annotations with the given
		 * {@code annotationNames}.
		 * @param name the name of the class
		 */
		ApplicationClass(String name) {
			this.name = name;

		}

		String getName() {
			return this.name;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			ApplicationClass other = (ApplicationClass) obj;
			return this.name.equals(other.name);
		}

		@Override
		public int hashCode() {
			return this.name.hashCode();
		}

		@Override
		public String toString() {
			return this.name;
		}

	}

	/**
	 * Find a single annotated class, throwing an {@link IllegalStateException} if multiple
	 * candidates exist.
	 */
	private static final class SingleAnnotatedClassCallback implements AnnotatedClassFinder.AnnotatedClassCallback<Object> {

		private final Set<ApplicationClass> applicationClasses = new LinkedHashSet<>();

		private SingleAnnotatedClassCallback() {
		}

		@Override
		public Object doWith(ApplicationClass applicationClass) {
			this.applicationClasses.add(applicationClass);
			return null;
		}

		private String getAnnotatedClassName() {
			Set<ApplicationClass> matchingApplicationClasses = new LinkedHashSet<>();
			if (matchingApplicationClasses.isEmpty()) {
				matchingApplicationClasses.addAll(this.applicationClasses);
			}
			if (matchingApplicationClasses.size() > 1) {
				throw new IllegalStateException(
						"Unable to find a single annotated class from the following candidates " + matchingApplicationClasses);
			}
			return (matchingApplicationClasses.isEmpty() ? null : matchingApplicationClasses.iterator().next().getName());
		}

	}
}
