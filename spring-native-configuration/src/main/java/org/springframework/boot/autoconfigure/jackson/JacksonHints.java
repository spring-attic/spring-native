/*
 * Copyright 2021 the original author or authors.
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
package org.springframework.boot.autoconfigure.jackson;

import com.fasterxml.jackson.databind.ext.Java7Handlers;
import com.fasterxml.jackson.databind.ext.Java7HandlersImpl;
import com.fasterxml.jackson.databind.ext.Java7Support;
import com.fasterxml.jackson.databind.ext.Java7SupportImpl;
import com.fasterxml.jackson.databind.util.ClassUtil;
import org.springframework.nativex.extension.*;
import org.springframework.nativex.type.AccessBits;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;

@NativeHint(trigger = JacksonAutoConfiguration.class, typeInfos = {
		@TypeInfo(types = {JsonIgnore.class, JsonInclude.class,JsonInclude.Include.class},access=AccessBits.CLASS|AccessBits.DECLARED_METHODS),
		@TypeInfo(types = JsonGenerator.class) },
		initializationInfos = @InitializationInfo(types = {Java7Handlers.class, Java7HandlersImpl.class, Java7Support.class, Java7SupportImpl.class, ClassUtil.class}, initTime = InitializationTime.BUILD))
public class JacksonHints implements NativeConfiguration {
}
