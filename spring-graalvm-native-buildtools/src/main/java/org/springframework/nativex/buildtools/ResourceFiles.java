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
