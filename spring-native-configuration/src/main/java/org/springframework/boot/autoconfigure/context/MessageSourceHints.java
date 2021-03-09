package org.springframework.boot.autoconfigure.context;

import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.hint.AccessBits;

@NativeHint(trigger = MessageSourceAutoConfiguration.class,
	types = @TypeHint(types= MessageSourceProperties.class, access = AccessBits.CLASS | AccessBits.DECLARED_METHODS))
public class MessageSourceHints implements NativeConfiguration {
}
