package org.springframework.boot.test.autoconfigure.web.reactive;

import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = WebTestClientAutoConfiguration.class, resources =
		// TODO Use @TypeHint when https://github.com/spring-projects-experimental/spring-native/issues/813 will be fixed
		@ResourceHint(patterns = "org/springframework/boot/test/autoconfigure/web/reactive/WebTestClientAutoConfiguration.class"))
public class WebTestClientHints implements NativeConfiguration {
}
