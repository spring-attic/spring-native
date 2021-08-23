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

package org.springframework.boot.autoconfigure.condition;

import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger=ConditionalOnWebApplication.class, abortIfTypesMissing = true)
@NativeHint(trigger = ConditionalOnSingleCandidate.class, extractTypesFromAttributes = { "value", "type" }, abortIfTypesMissing = true)
@NativeHint(trigger = ConditionalOnClass.class, extractTypesFromAttributes = { "value", "name" }, abortIfTypesMissing = true)
// Here exposing SearchStrategy as it is the type of a field within the annotation. 
// TODO Feels like all conditions should just get this 'for free'
@NativeHint( trigger = ConditionalOnMissingBean.class, abortIfTypesMissing = true)
@NativeHint(trigger = ConditionalOnBean.class, extractTypesFromAttributes = {"value", "type"},abortIfTypesMissing = true)
public class ConditionalHints implements NativeConfiguration {
}
