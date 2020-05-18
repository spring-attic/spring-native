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
package org.springframework.boot.autoconfigure.condition;

import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.context.support.WebApplicationObjectSupport;

@NativeImageHint(trigger=OnWebApplicationCondition.class,typeInfos = {
	@TypeInfo(types= {GenericWebApplicationContext.class,WebApplicationObjectSupport.class,ApplicationObjectSupport.class})	
})
@NativeImageHint(trigger=ConditionalOnWebApplication.class, 
	typeInfos= {
		@TypeInfo(types= {GenericWebApplicationContext.class, ConditionalOnWebApplication.Type.class})},
				  abortIfTypesMissing = true)
@NativeImageHint(trigger = ConditionalOnSingleCandidate.class,
	extractTypesFromAttributes = { "value", "type" }, abortIfTypesMissing = true)
@NativeImageHint(trigger = ConditionalOnClass.class,
	extractTypesFromAttributes = { "value", "name" }, abortIfTypesMissing = true)
// Here exposing SearchStrategy as it is the type of a field within the annotation. 
// TODO Feels like all conditions should just get this 'for free'
@NativeImageHint(trigger = ConditionalOnMissingBean.class,
	extractTypesFromAttributes = { "value", "name" },
	typeInfos = { @TypeInfo(types= {SearchStrategy.class}) }, abortIfTypesMissing = true)
public class Hints implements NativeImageConfiguration {
}
