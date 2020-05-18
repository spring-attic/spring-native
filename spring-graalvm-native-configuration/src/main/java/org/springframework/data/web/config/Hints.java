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
package org.springframework.data.web.config;

import org.springframework.data.web.config.EnableSpringDataWebSupport.QuerydslActivator;
import org.springframework.data.web.config.EnableSpringDataWebSupport.SpringDataWebConfigurationImportSelector;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.TypeInfo;


/*
//  EnableSpringDataWebSupport. TODO: there are others in spring.factories.
proposedHints.put(SpringDataWebConfigurationSelector,
		new CompilationHint(true, true, new String[] {
			"org.springframework.data.web.config.HateoasAwareSpringDataWebConfiguration",
			"org.springframework.data.web.config.SpringDataWebConfiguration"
		}));
*/
@NativeImageHint(trigger = SpringDataWebConfigurationImportSelector.class, typeInfos = {
	@TypeInfo(types= {
			HateoasAwareSpringDataWebConfiguration.class,
			SpringDataWebConfiguration.class,
			ProjectingArgumentResolverRegistrar.class,
			SpringDataJacksonConfiguration.class
	})
},abortIfTypesMissing = true,follow=true) 
/*
public final static String SpringDataWebQueryDslSelector = "Lorg/springframework/data/web/config/EnableSpringDataWebSupport$QuerydslActivator;";
//  EnableSpringDataWebSupport. TODO: there are others in spring.factories.
proposedHints.put(SpringDataWebQueryDslSelector,
		new CompilationHint(true, true, new String[] {
			"org.springframework.data.web.config.QuerydslWebConfiguration"}
		));
*/
@NativeImageHint(trigger = QuerydslActivator.class, typeInfos = {
	@TypeInfo(types= {QuerydslWebConfiguration.class})	
},abortIfTypesMissing = true,follow=true) 
public class Hints implements NativeImageConfiguration {
}
