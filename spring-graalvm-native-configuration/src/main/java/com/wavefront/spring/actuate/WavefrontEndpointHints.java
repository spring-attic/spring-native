package com.wavefront.spring.actuate;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpoint;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;

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
