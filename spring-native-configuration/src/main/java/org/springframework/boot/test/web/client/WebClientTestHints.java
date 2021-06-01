package org.springframework.boot.test.web.client;

import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = TestRestTemplateContextCustomizerFactory.class, types = @TypeHint(typeNames =
		{
				"org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer$TestRestTemplateRegistrar",
				"org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer$TestRestTemplateFactory"
		}))
public class WebClientTestHints implements NativeConfiguration {
}
