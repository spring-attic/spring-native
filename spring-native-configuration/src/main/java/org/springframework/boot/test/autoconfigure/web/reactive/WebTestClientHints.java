package org.springframework.boot.test.autoconfigure.web.reactive;

import org.springframework.nativex.hint.Flag;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = WebTestClientAutoConfiguration.class, types =
		@TypeHint(types = WebTestClientAutoConfiguration.class, access = Flag.resource))
public class WebTestClientHints implements NativeConfiguration {
}
