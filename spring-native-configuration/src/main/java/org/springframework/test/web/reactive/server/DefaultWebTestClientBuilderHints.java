package org.springframework.test.web.reactive.server;

import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = DefaultWebTestClientBuilder.class, initialization = @InitializationHint(
		types = DefaultWebTestClientBuilder.class, initTime = InitializationTime.BUILD
))
public class DefaultWebTestClientBuilderHints implements NativeConfiguration {
}
