package org.springframework.boot.test.autoconfigure.web.reactive;

import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = WebTestClientAutoConfiguration.class, types =
		@TypeHint(types = WebTestClientAutoConfiguration.class, access = AccessBits.RESOURCE))
public class WebTestClientHints implements NativeConfiguration {
}
