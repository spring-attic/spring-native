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

package org.springframework.cloud.config.client;

import org.springframework.cloud.bootstrap.BootstrapImportSelector;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = ConfigClientAutoConfiguration.class,
		options = "--enable-http",
		types = @TypeHint(types = {Environment.class, PropertySource.class, ConfigClientProperties.class }, access = AccessBits.ALL))
@NativeHint(trigger = BootstrapImportSelector.class, types = @TypeHint(types = ConfigServiceBootstrapConfiguration.class))
public class ConfigClientHints implements NativeConfiguration {
}