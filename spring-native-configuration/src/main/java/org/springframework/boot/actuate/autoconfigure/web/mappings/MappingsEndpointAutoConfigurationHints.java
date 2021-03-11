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

package org.springframework.boot.actuate.autoconfigure.web.mappings;

import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;

// Hitting /mappings endpoint
@NativeHint(trigger = MappingsEndpointAutoConfiguration.class, types = {
	@TypeHint(types = {
		org.springframework.boot.actuate.web.mappings.HandlerMethodDescription.class,
		org.springframework.boot.actuate.web.mappings.MappingsEndpoint.ApplicationMappings.class,
		org.springframework.boot.actuate.web.mappings.MappingsEndpoint.ContextMappings.class,
		org.springframework.boot.actuate.web.mappings.servlet.DispatcherServletMappingDescription.class,
		org.springframework.boot.actuate.web.mappings.servlet.DispatcherServletMappingDetails.class,
		org.springframework.boot.actuate.web.mappings.servlet.FilterRegistrationMappingDescription.class,
		org.springframework.boot.actuate.web.mappings.servlet.RegistrationMappingDescription.class,
		org.springframework.boot.actuate.web.mappings.servlet.RequestMappingConditionsDescription.class,
		org.springframework.boot.actuate.web.mappings.servlet.RequestMappingConditionsDescription.MediaTypeExpressionDescription.class,
		org.springframework.boot.actuate.web.mappings.servlet.ServletRegistrationMappingDescription.class,
		org.springframework.web.bind.annotation.RequestMethod.class
	})
})
public class MappingsEndpointAutoConfigurationHints implements NativeConfiguration {
}