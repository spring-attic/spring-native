package org.springframework.messaging;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.nativex.extension.NativeConfiguration;
import org.springframework.nativex.extension.NativeHint;
import org.springframework.nativex.extension.TypeInfo;
import org.springframework.nativex.type.AccessBits;

@NativeHint(typeInfos = {
		@TypeInfo(types= {
				MessageMapping.class
		},access= AccessBits.CLASS|AccessBits.DECLARED_METHODS)
})
public class MessagingAnnotationHints implements NativeConfiguration {
}
