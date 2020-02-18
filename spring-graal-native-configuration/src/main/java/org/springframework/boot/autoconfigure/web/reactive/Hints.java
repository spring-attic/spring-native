package org.springframework.boot.autoconfigure.web.reactive;

import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.graal.extension.TypeInfo;
import org.springframework.graal.type.AccessBits;
import org.springframework.http.codec.support.DefaultClientCodecConfigurer;
import org.springframework.http.codec.support.DefaultServerCodecConfigurer;
import org.springframework.util.ClassUtils;

@ConfigurationHint(value=WebFluxAutoConfiguration.class,typeInfos = {
	// These two believed through WebFluxConfigurationSupport, CodecConfigurer.properties
	@TypeInfo(types= {DefaultClientCodecConfigurer.class,DefaultServerCodecConfigurer.class},
			// These are from BaseDefaultCodecs - not sure on needed visibility
			// TODO Aren't these also needed for non reactive auto configuration web? Is there a common configuration supertype between those
			// configurations that they can be hung off
			typeNames= {
				"com.fasterxml.jackson.databind.ObjectMapper", 
				"com.fasterxml.jackson.core.JsonGenerator",
				"com.fasterxml.jackson.dataformat.smile.SmileFactory", 
				"javax.xml.bind.Binder", 
				"com.google.protobuf.Message", 
				"org.synchronoss.cloud.nio.multipart.NioMultipartParser"
			},
			access=AccessBits.CLASS|AccessBits.PUBLIC_CONSTRUCTORS
		)
})
public class Hints implements NativeImageConfiguration {
}
