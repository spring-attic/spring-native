package org.springframework.messaging;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.nativex.extension.NativeImageConfiguration;
import org.springframework.nativex.extension.NativeImageHint;
import org.springframework.nativex.extension.TypeInfo;
import org.springframework.nativex.type.AccessBits;

@NativeImageHint(typeInfos = {
		@TypeInfo(types= {
				MessageMapping.class
		},access= AccessBits.CLASS|AccessBits.DECLARED_METHODS)
})
public class MessagingAnnotationHints implements NativeImageConfiguration {
}
