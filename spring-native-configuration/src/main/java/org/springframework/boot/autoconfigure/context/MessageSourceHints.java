package org.springframework.boot.autoconfigure.context;

import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeInfo;
import org.springframework.nativex.hint.AccessBits;

@NativeHint(trigger = MessageSourceAutoConfiguration.class,
	typeInfos = 
		@TypeInfo(types= MessageSourceProperties.class, access = AccessBits.CLASS|AccessBits.DECLARED_METHODS))
public class MessageSourceHints implements NativeConfiguration {
}
