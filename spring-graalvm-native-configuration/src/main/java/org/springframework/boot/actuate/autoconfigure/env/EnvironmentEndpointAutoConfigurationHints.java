/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.boot.actuate.autoconfigure.env;

import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;

// Hitting /env endpoint
@NativeImageHint(trigger = EnvironmentEndpointAutoConfiguration.class, typeInfos = { 
	@TypeInfo(types = {
		EnvironmentEndpoint.class,
		org.springframework.boot.actuate.env.EnvironmentEndpoint.EnvironmentDescriptor.class,
		org.springframework.boot.actuate.env.EnvironmentEndpoint.PropertySourceDescriptor.class,
		org.springframework.boot.actuate.env.EnvironmentEndpoint.PropertyValueDescriptor.class
	},access=AccessBits.LOAD_AND_CONSTRUCT_AND_PUBLIC_METHODS)
})
public class EnvironmentEndpointAutoConfigurationHints implements NativeImageConfiguration {
}