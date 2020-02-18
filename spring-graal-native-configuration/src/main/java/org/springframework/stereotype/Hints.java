package org.springframework.stereotype;

import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.graal.extension.TypeInfo;
import org.springframework.graal.type.AccessBits;

@ConfigurationHint(typeInfos = {
	@TypeInfo(types= {Controller.class
			},access=AccessBits.CLASS|AccessBits.PUBLIC_METHODS)
})
public class Hints implements NativeImageConfiguration {
}
