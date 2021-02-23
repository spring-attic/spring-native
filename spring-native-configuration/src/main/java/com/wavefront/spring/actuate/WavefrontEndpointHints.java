package com.wavefront.spring.actuate;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpoint;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeInfo;
import org.springframework.nativex.hint.AccessBits;

@NativeHint(trigger = WavefrontEndpointAutoConfiguration.class, types = {
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
public class WavefrontEndpointHints implements NativeConfiguration {
}
