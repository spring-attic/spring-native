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
package org.springframework.hateoas.config;

import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;

/*
proposedHints.put(WebStackImportSelector,
		new CompilationHint(false, true, new String[] {
			//"org.springframework.hateoas.config.WebStackImportSelector" - why was this here???
			"org.springframework.hateoas.config.WebMvcHateoasConfiguration",
			"org.springframework.hateoas.config.WebFluxHateoasConfiguration"
		}));
		*/
@NativeImageHint(trigger=WebStackImportSelector.class,typeInfos= {
	@TypeInfo(types= {WebMvcHateoasConfiguration.class,WebFluxHateoasConfiguration.class})	
},follow=true)
/*
public final static String HypermediaConfigurationImportSelector = "Lorg/springframework/hateoas/config/HypermediaConfigurationImportSelector;";
	// TODO I am not sure the specific entry here is right, but given that the selector references entries loaded via factories - aren't those already handled? 
	proposedHints.put(HypermediaConfigurationImportSelector,
			new CompilationHint(false, true, new String[] {
					"org.springframework.hateoas.config.HypermediaConfigurationImportSelector"
			}));
			*/
@NativeImageHint(trigger=HypermediaConfigurationImportSelector.class,typeInfos= {
	@TypeInfo(types= {
		HypermediaConfigurationImportSelector.class,
		EnableHypermediaSupport.class, HypermediaType.class, HypermediaType[].class,
		MediaTypeConfigurationProvider.class
	})})
public class Hints implements NativeImageConfiguration {
}
