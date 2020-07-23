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
package org.springframework.boot.context.properties;

import java.util.ArrayList;

import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;

@NativeImageHint(trigger = EnableConfigurationPropertiesRegistrar.class, typeInfos = {
	@TypeInfo(types= {
			BoundConfigurationProperties.class,
			ConfigurationPropertiesBindingPostProcessor.class,
			ConfigurationPropertiesBinder.Factory.class,
			ConfigurationPropertiesBinder.class,
			DeprecatedConfigurationProperty.class,
			NestedConfigurationProperty.class,
			ArrayList.class
			}, typeNames = {
			"java.io.Serializable[]",
			"java.lang.Comparable[]",
			"java.lang.CharSequence[]",
			"java.lang.String[]"
	})
})
@NativeImageHint(trigger = EnableConfigurationProperties.class,
	typeInfos={@TypeInfo(types= {ConstructorBinding.class})}
)
@NativeImageHint(typeInfos = {
		@TypeInfo(types= {
				ConfigurationPropertiesScan.class,
				ConfigurationPropertiesScanRegistrar.class,
				ConfigurationBeanFactoryMetadata.class
		})
})
public class ConfigurationPropertiesHints implements NativeImageConfiguration {
}
