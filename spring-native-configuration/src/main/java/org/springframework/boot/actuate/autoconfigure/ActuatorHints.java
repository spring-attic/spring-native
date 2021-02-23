package org.springframework.boot.actuate.autoconfigure;

import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ProxyInfo;

@NativeHint(trigger = org.springframework.boot.actuate.endpoint.annotation.Endpoint.class,
		proxies = {
		@ProxyInfo(types = {
				org.springframework.boot.actuate.endpoint.annotation.Endpoint.class,
				org.springframework.core.annotation.SynthesizedAnnotation.class
		}), @ProxyInfo(types = {
				org.springframework.boot.actuate.endpoint.annotation.EndpointExtension.class,
				org.springframework.core.annotation.SynthesizedAnnotation.class
		})
})
public class ActuatorHints implements NativeConfiguration {
}
