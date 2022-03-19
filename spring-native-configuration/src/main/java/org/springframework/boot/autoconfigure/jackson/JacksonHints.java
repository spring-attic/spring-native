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

package org.springframework.boot.autoconfigure.jackson;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ext.Java7Handlers;
import com.fasterxml.jackson.databind.ext.Java7HandlersImpl;
import com.fasterxml.jackson.databind.ext.Java7Support;
import com.fasterxml.jackson.databind.ext.Java7SupportImpl;
import com.fasterxml.jackson.databind.util.ClassUtil;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = JacksonAutoConfiguration.class, types = {
		@TypeHint(types = {
				JsonPropertyOrder.class, JsonCreator.class,
				JsonDeserialize.class, JsonProperty.class,
				JsonGetter.class, JsonSetter.class,
				JsonValue.class, JsonView.class,
				JsonTypeInfo.class, JsonIgnoreProperties.class,
				JsonAnyGetter.class, JsonAnySetter.class,
				JsonSerialize.class, JsonUnwrapped.class,
				JsonIgnore.class, JsonInclude.class,
				JsonInclude.Include.class}),
		@TypeHint(types = JsonGenerator.class, access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.PUBLIC_METHODS}),
		@TypeHint(types = PropertyNamingStrategies.class, access = { TypeAccess.PUBLIC_FIELDS })},
		initialization = @InitializationHint(types = {Java7Handlers.class, Java7HandlersImpl.class, Java7Support.class, Java7SupportImpl.class, ClassUtil.class}, initTime = InitializationTime.BUILD))
public class JacksonHints implements NativeConfiguration {

}
