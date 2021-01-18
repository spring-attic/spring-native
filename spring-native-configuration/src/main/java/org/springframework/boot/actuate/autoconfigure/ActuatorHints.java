package org.springframework.boot.actuate.autoconfigure;

import org.springframework.nativex.extension.NativeImageConfiguration;
import org.springframework.nativex.extension.NativeImageHint;
import org.springframework.nativex.extension.ProxyInfo;

@NativeImageHint(trigger = org.springframework.boot.actuate.endpoint.annotation.Endpoint.class,
		proxyInfos = {
		@ProxyInfo(types = {
				org.springframework.boot.actuate.endpoint.annotation.Endpoint.class,
				org.springframework.core.annotation.SynthesizedAnnotation.class
		}), @ProxyInfo(types = {
				org.springframework.boot.actuate.endpoint.annotation.EndpointExtension.class,
				org.springframework.core.annotation.SynthesizedAnnotation.class
		})
})
public class ActuatorHints implements NativeImageConfiguration {
}
