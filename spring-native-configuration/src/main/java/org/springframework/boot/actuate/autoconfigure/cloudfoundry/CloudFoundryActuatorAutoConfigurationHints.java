package org.springframework.boot.actuate.autoconfigure.cloudfoundry;

import org.springframework.boot.actuate.autoconfigure.cloudfoundry.reactive.ReactiveCloudFoundryActuatorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.cloudfoundry.servlet.CloudFoundryActuatorAutoConfiguration;
import org.springframework.nativex.hint.Flag;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = ReactiveCloudFoundryActuatorAutoConfiguration.class, types = {
		@TypeHint(types = EndpointCloudFoundryExtension.class),
		@TypeHint(typeNames = "org.springframework.boot.actuate.autoconfigure.cloudfoundry.CloudFoundryEndpointFilter"),
		@TypeHint(typeNames = "org.springframework.boot.actuate.autoconfigure.cloudfoundry.reactive.CloudFoundryWebFluxEndpointHandlerMapping$CloudFoundryLinksHandler", access = { Flag.allDeclaredConstructors, Flag.allDeclaredMethods })
})
@NativeHint(trigger = CloudFoundryActuatorAutoConfiguration.class, types = {
		@TypeHint(types = EndpointCloudFoundryExtension.class),
		@TypeHint(typeNames = "org.springframework.boot.actuate.autoconfigure.cloudfoundry.CloudFoundryEndpointFilter"),
		@TypeHint(typeNames = "org.springframework.boot.actuate.autoconfigure.cloudfoundry.servlet.CloudFoundryWebEndpointServletHandlerMapping$CloudFoundryLinksHandler", access = { Flag.allDeclaredConstructors, Flag.allDeclaredMethods })
})
public class CloudFoundryActuatorAutoConfigurationHints implements NativeConfiguration {
}
