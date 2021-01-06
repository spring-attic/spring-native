package org.springframework.nativex.buildtools;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Brian Clozel
 */
public interface ResourceFile extends GeneratedFile {

	Path MAIN_RESOURCES_PATH = Paths.get("src", "main", "resources");

}
