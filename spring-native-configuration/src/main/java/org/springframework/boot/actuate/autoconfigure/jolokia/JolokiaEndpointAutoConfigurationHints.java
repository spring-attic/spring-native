/*
 * Copyright 2021 the original author or authors.
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
package org.springframework.boot.actuate.autoconfigure.jolokia;

import org.jolokia.http.AgentServlet;
import org.jolokia.http.Jsr160ProxyNotEnabledByDefaultAnymoreDispatcher;
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpoint;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

// Hitting /jolokia endpoint
@NativeHint(trigger = JolokiaEndpointAutoConfiguration.class, types = {
	@TypeHint(types = {
		Jsr160ProxyNotEnabledByDefaultAnymoreDispatcher.class,
		AgentServlet.class
	},access=AccessBits.LOAD_AND_CONSTRUCT),
	@TypeHint(types = ServletEndpoint.class, access=AccessBits.DECLARED_METHODS)
})
public class JolokiaEndpointAutoConfigurationHints implements NativeConfiguration {
}