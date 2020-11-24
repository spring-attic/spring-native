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
package org.springframework.boot.actuate.autoconfigure.info;

import org.springframework.nativex.extension.NativeImageConfiguration;

// TODO tests or not point keeping it...
// Hitting /info endpoint
//@NativeImageHint(trigger = InfoEndpointAutoConfiguration.class, typeInfos = { 
////	@TypeInfo(types = {
//////		EnvironmentInfoContributor.class,
//////		InfoContributor.class,
//////		InfoEndpoint.class
////	},access=AccessBits.LOAD_AND_CONSTRUCT_AND_PUBLIC_METHODS)
//})
public class InfoEndpointAutoConfigurationHints implements NativeImageConfiguration {
}