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

package org.springframework.boot.autoconfigure;

import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.hint.AccessBits;


@NativeHint(trigger = ImportAutoConfigurationImportSelector.class, types =
		{
				@TypeHint(types = ImportAutoConfiguration.class),
				@TypeHint(types = ImportAutoConfigurationImportSelector.class, access = AccessBits.LOAD_AND_CONSTRUCT | AccessBits.RESOURCE)
		}
)
@NativeHint(trigger = AutoConfigurationImportSelector.class)
@TypeHint(types = { AutoConfigureBefore.class, AutoConfigureAfter.class, AutoConfigureOrder.class, AutoConfigurationPackage.class },
  access = AccessBits.CLASS | AccessBits.DECLARED_METHODS) 
// TODO why isn't this one pulled in via @EnableAutoConfiguration handling?
@NativeHint(types = {
	@TypeHint(types = {
			AutoConfigurationImportSelector.class,
			AutoConfigurationPackages.class,
			AutoConfigurationPackages.Registrar.class,
			AutoConfigurationPackages.BasePackages.class,
			EnableAutoConfiguration.class,
			SpringBootApplication.class
	}, access = AccessBits.LOAD_AND_CONSTRUCT | AccessBits.PUBLIC_METHODS)
})
@NativeHint(types = {
	@TypeHint(typeNames = {
		"org.springframework.boot.autoconfigure.AutoConfigurationImportSelector$AutoConfigurationGroup" 
	})
})
public class AutoConfigurationHints implements NativeConfiguration {
}