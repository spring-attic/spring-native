/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.actuate.autoconfigure.endpoint.web.servlet;

import org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextFactory;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextType;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.actuate.endpoint.web.PathMapper;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointDiscoverer;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.type.TypeSystem;

// The configurations related to actuator are in this key in spring.factories:
// org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration
// NOT the EnableAutoConfiguration key. There is special handling of the ManagementContextConfiguration
// key that will handle hints against these (so we can use a ...ContextConfiguration trigger class
// here - we don't have to use a standard auto config visible through EnableAutoConfiguration).
// If we wanted to use a standard config, we'd trigger on WebEndpointAutoConfiguration
@NativeHint(trigger = WebMvcEndpointManagementContextConfiguration.class,
	imports = CommonWebActuatorTypes.class,
 	types = {
		@TypeHint(types = PathMappedEndpoints.class),
 		@TypeHint(types = {
 				ControllerEndpointDiscoverer.class,
				ControllerEndpointsSupplier.class,
				ManagementContextType.class,
				EndpointMediaTypes.class,
				WebEndpointsSupplier.class,
				EndpointWebExtension.class,
				WebEndpoint.class,
				// web package
				ManagementContextConfiguration.class,
				ManagementContextFactory.class,
				ManagementContextType.class,
				PathMapper.class,
				ManagementPortType.class,
		}, typeNames = {
			"org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping$LinksHandler",
			"org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping$OperationHandler",
			"org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping$WebMvcLinksHandler",
	})
})
public class WebMvcEndpointManagementContextConfigurationHints implements NativeConfiguration {

	@Override
	public boolean isValid(TypeSystem typeSystem) {
		// Similar to check in OnWebApplicationCondition (effectively implementing ConditionalOnWebApplication(SERVLET))
		boolean isWebMvcApplication = typeSystem.resolveName("org.springframework.web.context.support.GenericWebApplicationContext", true) != null;
		return isWebMvcApplication;
	}

}