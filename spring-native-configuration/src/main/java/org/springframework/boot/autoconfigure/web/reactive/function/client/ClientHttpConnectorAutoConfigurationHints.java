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

package org.springframework.boot.autoconfigure.web.reactive.function.client;

import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = ClientHttpConnectorAutoConfiguration.class,
		types = @TypeHint(typeNames = {
				"org.springframework.boot.autoconfigure.web.reactive.function.client.ClientHttpConnectorConfiguration$ReactorNetty",
				"org.springframework.boot.autoconfigure.web.reactive.function.client.ClientHttpConnectorConfiguration$JettyClient",
				"org.springframework.boot.autoconfigure.web.reactive.function.client.ClientHttpConnectorConfiguration$HttpClient5"
		}, access = AccessBits.QUERY_DECLARED_METHODS)
)
public class ClientHttpConnectorAutoConfigurationHints implements NativeConfiguration {
}
