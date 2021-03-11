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

package com.wavefront.spring.actuate;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpoint;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.hint.AccessBits;

@NativeHint(trigger = WavefrontEndpointAutoConfiguration.class, types = {
		@TypeHint(types = {
				WavefrontController.class
		}, typeNames = {
				"org.springframework.boot.actuate.autoconfigure.endpoint.condition.OnAvailableEndpointCondition",
				"org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointFilter"
		}),
		@TypeHint(types = {
				ConditionalOnAvailableEndpoint.class,
				ControllerEndpoint.class
		}, access = AccessBits.LOAD_AND_CONSTRUCT | AccessBits.DECLARED_METHODS)
})
public class WavefrontEndpointHints implements NativeConfiguration {
}
