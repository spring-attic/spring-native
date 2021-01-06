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
package org.springframework.boot.context.config;

import org.springframework.boot.context.properties.bind.Name;
import org.springframework.nativex.extension.NativeImageConfiguration;
import org.springframework.nativex.extension.NativeImageHint;
import org.springframework.nativex.extension.TypeInfo;
import org.springframework.nativex.type.AccessBits;

@NativeImageHint(typeInfos = {
	@TypeInfo(types = ConfigDataLocation.class,access=AccessBits.DECLARED_METHODS),
	@TypeInfo(types = ConfigDataLocation[].class,access=AccessBits.CLASS),
	@TypeInfo(types = {
		ConfigDataEnvironmentPostProcessor.class,ConfigTreeConfigDataLoader.class,
		ConfigTreeConfigDataLocationResolver.class, DelegatingApplicationContextInitializer.class,
		DelegatingApplicationListener.class, StandardConfigDataLoader.class,
		StandardConfigDataLocationResolver.class
	},access=AccessBits.DECLARED_CONSTRUCTORS),
	@TypeInfo(types= {
		ConfigDataProperties.class,
		ConfigDataProperties.Activate.class,
		Name.class
	})
})
public class ConfigDataPropertiesHints implements NativeImageConfiguration {
}
