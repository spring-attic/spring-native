package org.springframework.nativex.buildtools;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Brian Clozel
 */
public interface SourceFile extends GeneratedFile {

	Path MAIN_SRC_PATH = Paths.get("src", "main", "java");

}
