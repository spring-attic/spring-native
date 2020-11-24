package com.wavefront.spring.actuate;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpoint;
import org.springframework.nativex.extension.NativeImageConfiguration;
import org.springframework.nativex.extension.NativeImageHint;
import org.springframework.nativex.extension.TypeInfo;
import org.springframework.nativex.type.AccessBits;

@NativeImageHint(trigger = WavefrontEndpointAutoConfiguration.class, typeInfos = {
		@TypeInfo(types = {
				WavefrontController.class
		}, typeNames = {
				"org.springframework.boot.actuate.autoconfigure.endpoint.condition.OnAvailableEndpointCondition",
				"org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointFilter"
		}),
		@TypeInfo(types = {
				ConditionalOnAvailableEndpoint.class,
				ControllerEndpoint.class
		}, access = AccessBits.LOAD_AND_CONSTRUCT|AccessBits.DECLARED_METHODS)
})
public class WavefrontEndpointHints implements NativeImageConfiguration {
}
