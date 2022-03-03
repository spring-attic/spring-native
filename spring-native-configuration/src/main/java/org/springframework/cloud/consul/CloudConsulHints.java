/*
 * Copyright 2019-2022 the original author or authors.
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

package org.springframework.cloud.consul;

import com.ecwid.consul.v1.kv.model.GetValue;

import org.springframework.cloud.consul.config.ConsulConfigAutoConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = ConsulConfigAutoConfiguration.class,
		options = {"--enable-http", "--enable-https"},
		types = @TypeHint(types = GetValue.class, access = {TypeAccess.PUBLIC_CONSTRUCTORS,
				TypeAccess.DECLARED_FIELDS}))
public class CloudConsulHints implements NativeConfiguration {
}
