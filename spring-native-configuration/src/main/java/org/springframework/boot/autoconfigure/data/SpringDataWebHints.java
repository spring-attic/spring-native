package org.springframework.boot.autoconfigure.data;

import org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration;
import org.springframework.data.mongodb.config.GeoJsonConfiguration;
import org.springframework.nativex.extension.NativeConfiguration;
import org.springframework.nativex.extension.NativeHint;
import org.springframework.nativex.extension.TypeInfo;
import org.springframework.nativex.type.AccessBits;

@NativeHint(trigger = SpringDataWebAutoConfiguration.class, typeInfos =
		@TypeInfo(types = GeoJsonConfiguration.class, access = AccessBits.CONFIGURATION))
public class SpringDataWebHints implements NativeConfiguration {
}
