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

package org.springframework.aot;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.squareup.javapoet.JavaFile;

import org.springframework.util.StreamUtils;

/**
 * Factory methods for creating {@link SourceFile} instances from various inputs.
 *
 * @author Brian Clozel
 */
public abstract class SourceFiles {

	/**
	 * Create a {@link SourceFile} from a {@link JavaFile} generated with Javapoet.
	 * @param javaFile the java file
	 * @return the source file
	 */
	public static SourceFile fromJavaFile(JavaFile javaFile) {
		return rootPath -> javaFile.writeTo(rootPath);
	}

	/**
	 * Create a {@link SourceFile} from an {@link InputStream}, usually associated
	 * with a static resource on disk.
	 * @param packageName the package name
	 * @param className the class name
	 * @param staticFile the static file
	 * @return the source file
	 */
	public static SourceFile fromStaticFile(String packageName, String className, InputStream staticFile) {
		return rootPath -> {
			Path packagePath = rootPath;
			for (String segment : packageName.split(("\\."))) {
				packagePath = packagePath.resolve(segment);
			}
			Files.createDirectories(packagePath);
			Path outputPath = packagePath.resolve(Paths.get(className + ".java"));
			StreamUtils.copy(staticFile, Files.newOutputStream(outputPath));
		};
	}
	
}
