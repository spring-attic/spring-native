package org.springframework.boot.logging;

import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;

// TODO: This is only needed in functional mode (why?) but there's no way to specify that currently
@NativeImageHint(typeInfos =
		@TypeInfo(types= {
				LogLevel.class
}))
public class LogLevelHints implements NativeImageConfiguration {
}
