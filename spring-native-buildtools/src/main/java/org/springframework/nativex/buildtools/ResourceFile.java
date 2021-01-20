package org.springframework.nativex.buildtools;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Brian Clozel
 */
public interface ResourceFile extends GeneratedFile {

	Path MAIN_RESOURCES_PATH = Paths.get("src", "main", "resources");

	Path NATIVE_IMAGE_PATH = MAIN_RESOURCES_PATH.resolve(Paths.get("META-INF", "native-image"));

	Path NATIVE_CONFIG_PATH = NATIVE_IMAGE_PATH.resolve(Paths.get("org.springframework.nativex", "buildtools"));

}
