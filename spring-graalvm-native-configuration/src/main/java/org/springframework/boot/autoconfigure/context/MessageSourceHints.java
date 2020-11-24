package org.springframework.boot.autoconfigure.context;

import org.springframework.nativex.extension.NativeImageConfiguration;
import org.springframework.nativex.extension.NativeImageHint;
import org.springframework.nativex.extension.TypeInfo;
import org.springframework.nativex.type.AccessBits;

@NativeImageHint(trigger = MessageSourceAutoConfiguration.class,
	typeInfos = 
		@TypeInfo(types= MessageSourceProperties.class, access = AccessBits.CLASS|AccessBits.DECLARED_METHODS))
public class MessageSourceHints implements NativeImageConfiguration  {
}
