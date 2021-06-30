package org.slf4j;

import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;


@NativeHint(
	trigger = SLF4JBridgeHandler.class,
		types = @TypeHint(types = {
				SLF4JBridgeHandler.class
		}, access = AccessBits.CLASS)
)
public class JulToSlf4jHints implements NativeConfiguration {
}
