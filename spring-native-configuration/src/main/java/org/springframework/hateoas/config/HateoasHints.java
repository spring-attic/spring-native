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

package org.springframework.hateoas.config;

import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;


@NativeHint(trigger=WebStackImportSelector.class, types = {
	@TypeHint(types= {
			WebMvcHateoasConfiguration.class,
			WebFluxHateoasConfiguration.class
	})
})
@NativeHint(trigger=HypermediaConfigurationImportSelector.class, types =
	@TypeHint(types= {
			HypermediaConfigurationImportSelector.class,
			EnableHypermediaSupport.class,
			HypermediaType.class,
			HypermediaType[].class,
			MediaTypeConfigurationProvider.class
	}))
public class HateoasHints implements NativeConfiguration {
}
