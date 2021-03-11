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

package org.springframework.boot.actuate.autoconfigure.endpoint.web.reactive;

import org.springframework.boot.actuate.autoconfigure.endpoint.web.servlet.CommonWebActuatorTypes;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.type.TypeSystem;

@NativeHint(trigger=WebFluxEndpointManagementContextConfiguration.class,
	imports = CommonWebActuatorTypes.class,
	types = @TypeHint(typeNames = {
		"org.springframework.boot.actuate.endpoint.web.reactive.WebFluxEndpointHandlerMapping$WebFluxLinksHandler",
		"org.springframework.boot.actuate.endpoint.web.reactive.AbstractWebFluxEndpointHandlerMapping$LinksHandler",
		"org.springframework.boot.actuate.endpoint.web.reactive.AbstractWebFluxEndpointHandlerMapping$ReadOperationHandler",
		"org.springframework.boot.actuate.autoconfigure.web.reactive.ReactiveManagementChildContextConfiguration",
		"org.springframework.boot.actuate.endpoint.web.reactive.AbstractWebFluxEndpointHandlerMapping$WriteOperationHandler",
		"org.springframework.boot.actuate.autoconfigure.web.reactive.ReactiveManagementContextFactory",
	}, access = AccessBits.LOAD_AND_CONSTRUCT | AccessBits.DECLARED_METHODS)
)
public class WebFluxEndpointManagementContextConfigurationHints implements NativeConfiguration {

	@Override
	public boolean isValid(TypeSystem typeSystem) {
		// Similar to check in OnWebApplicationCondition (effectively implementing ConditionalOnWebApplication(REACTIVE))
		boolean isWebfluxApplication = typeSystem.resolveName("org.springframework.web.reactive.HandlerResult", true) != null;
		return isWebfluxApplication;
	}

}