package org.springframework.boot.autoconfigure.context;

import org.springframework.nativex.extension.NativeConfiguration;
import org.springframework.nativex.extension.NativeHint;
import org.springframework.nativex.extension.TypeInfo;
import org.springframework.nativex.type.AccessBits;

@NativeHint(trigger = MessageSourceAutoConfiguration.class,
	typeInfos = 
		@TypeInfo(types= MessageSourceProperties.class, access = AccessBits.CLASS|AccessBits.DECLARED_METHODS))
public class MessageSourceHints implements NativeConfiguration {
}
