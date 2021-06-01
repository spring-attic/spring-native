package org.springframework.boot.test.web.reactive.server;

import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = org.springframework.boot.test.web.reactive.server.WebTestClientContextCustomizerFactory.class, types =
@TypeHint(typeNames = {
		"org.springframework.boot.test.web.reactive.server.WebTestClientContextCustomizer$WebTestClientRegistrar",
		"org.springframework.boot.test.web.reactive.server.WebTestClientContextCustomizer$WebTestClientFactory"
}))
public class ReactiveServerTestHints implements NativeConfiguration {
}
