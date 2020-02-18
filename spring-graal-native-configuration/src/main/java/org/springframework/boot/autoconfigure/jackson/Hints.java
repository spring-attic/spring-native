package org.springframework.boot.autoconfigure.jackson;

import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.graal.extension.TypeInfo;
import org.springframework.graal.type.AccessBits;

import com.fasterxml.jackson.core.JsonGenerator;

@ConfigurationHint(value=JacksonAutoConfiguration.class,typeInfos= {
		@TypeInfo(types= {JsonGenerator.class
				},access=AccessBits.CLASS|AccessBits.PUBLIC_METHODS|AccessBits.PUBLIC_CONSTRUCTORS)
	})
public class Hints implements NativeImageConfiguration {
}
