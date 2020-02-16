package org.springframework.boot.autoconfigure.web.reactive;

import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.graal.extension.TypeInfo;
import org.springframework.graal.type.AccessBits;
import org.springframework.http.codec.support.DefaultClientCodecConfigurer;
import org.springframework.http.codec.support.DefaultServerCodecConfigurer;

@ConfigurationHint(value=WebFluxAutoConfiguration.class,typeInfos = {
	// These two believed through WebFluxConfigurationSupport, CodecConfigurer.properties
	@TypeInfo(types= {DefaultClientCodecConfigurer.class,DefaultServerCodecConfigurer.class},
			access=AccessBits.CLASS|AccessBits.PUBLIC_CONSTRUCTORS
		)	
})
public class Hints implements NativeImageConfiguration {
}
