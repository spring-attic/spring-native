package org.springframework.messaging;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeInfo;
import org.springframework.nativex.hint.AccessBits;

@NativeHint(types = {
		@TypeInfo(types= {
				MessageMapping.class
		},access= AccessBits.CLASS|AccessBits.DECLARED_METHODS)
})
public class MessagingAnnotationHints implements NativeConfiguration {
}
