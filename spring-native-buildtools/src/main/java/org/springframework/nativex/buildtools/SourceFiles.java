package org.springframework.nativex.buildtools;

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
	 */
	public static SourceFile fromJavaFile(JavaFile javaFile) {
		return rootPath -> javaFile.writeTo(rootPath.resolve(SourceFile.MAIN_SRC_PATH));
	}

	/**
	 * Create a {@link SourceFile} from an {@link InputStream}, usually associated
	 * with a static resource on disk.
	 */
	public static SourceFile fromStaticFile(String packageName, String className, InputStream staticFile) {
		return rootPath -> {
			Path packagePath = rootPath.resolve(SourceFile.MAIN_SRC_PATH);
			for (String segment : packageName.split(("\\."))) {
				packagePath = packagePath.resolve(segment);
			}
			Files.createDirectories(packagePath);
			Path outputPath = packagePath.resolve(Paths.get(className + ".java"));
			StreamUtils.copy(staticFile, Files.newOutputStream(outputPath));
		};
	}
	
}
