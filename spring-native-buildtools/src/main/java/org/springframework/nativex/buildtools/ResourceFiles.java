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

package org.springframework.nativex.buildtools;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.util.StreamUtils;

/**
 * @author Brian Clozel
 */
public abstract class ResourceFiles {

	/**
	 * Create a {@link ResourceFile}, given a {@link Path} of an existing file
	 * and an optional relative path showing where the resource should be written under
	 * {@code "src/main/resources"}.
	 */
	public static ResourceFile fromStaticFile(Path sourcePath, Path relativeTo) {
		return rootPath -> {
			Path resourcePath = rootPath.resolve(ResourceFile.MAIN_RESOURCES_PATH);
			if (relativeTo != null) {
				resourcePath = resourcePath.resolve(relativeTo);
			}
			Files.createDirectories(resourcePath);
			resourcePath = resourcePath.resolve(sourcePath.getFileName());
			Files.copy(sourcePath, resourcePath, StandardCopyOption.REPLACE_EXISTING);
		};
	}

//	/**
//	 * Create a {@link ResourceFile}, given a {@link Path} relative path showing where
//	 * the resource should be written under {@code "src/main/resources"} and a file name
//	 * for the resource file, writing the data from the supplied input stream.
//	 */
//	public static ResourceFile fromInputStream(Path relativeTo, String filename, InputStream inputStream) {
//		return rootPath -> {
////			Path resourcePath = rootPath.resolve(ResourceFile.MAIN_RESOURCES_PATH);
//			Path resourcePath = rootPath.resolve(relativeTo);
//			Files.createDirectories(resourcePath);
//			resourcePath = resourcePath.resolve(Paths.get(filename));
//			StreamUtils.copy(inputStream, Files.newOutputStream(resourcePath));
//			inputStream.close();
//		};
//	}

}
