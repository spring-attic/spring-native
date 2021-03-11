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
import org.springframework.boot.actuate.endpoint.web.PathMapper;
import org.springframework.boot.actuate.endpoint.web.ServletEndpointRegistrar;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpointDiscoverer;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.hint.AccessBits;

/**
 * Example... this could host @TypeInfo annotations that any other @NativeHint could pull in
 * via an importInfos reference to this type.
 * 
 * @author Andy Clement
 */
@TypeHint(types = {
		ServletEndpointRegistrar.class,
		HttpTraceRepository.class,
		ManagementContextType.class,
		EndpointMediaTypes.class,
		WebEndpointsSupplier.class,
		EndpointWebExtension.class,
		WebEndpoint.class,
		WebEndpointDiscoverer.class,
		// web package
		ManagementContextConfiguration.class,
		ManagementContextFactory.class,
		ManagementContextType.class,
		PathMapper.class,
		ManagementPortType.class,
}, typeNames = {
		"org.springframework.boot.actuate.autoconfigure.web.server.EnableManagementContext",
		"org.springframework.boot.actuate.autoconfigure.web.server.EnableChildManagementContextConfiguration",
		"org.springframework.boot.actuate.endpoint.web.annotation.WebEndpointFilter",
	},access = AccessBits.LOAD_AND_CONSTRUCT | AccessBits.PUBLIC_METHODS)
public class CommonWebActuatorTypes {
}
