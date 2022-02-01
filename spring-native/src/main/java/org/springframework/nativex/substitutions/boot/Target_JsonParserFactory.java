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

package org.springframework.nativex.substitutions.boot;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.boot.json.BasicJsonParser;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.json.JsonParser;
import org.springframework.nativex.substitutions.OnlyIfPresent;
import org.springframework.nativex.substitutions.RemoveYamlSupport;
import org.springframework.util.ClassUtils;

/**
 * Why this substitution exists?
 * To avoid shipping YamlJsonParser in the native image when Yaml support is disabled via removeYamlSupport Spring AOT flag.
 *
 * How this substitution workarounds the problem?
 * It substitutes JsonParserFactory#getJsonParser with an alternative version without the related if clause when removeYamlSupport is true.
 *
 * Possible improvements
 * Perform all the ClassUtils#isPresent() checks (including Gson one) at build time.
 */
@TargetClass(className = "org.springframework.boot.json.JsonParserFactory", onlyWith = { RemoveYamlSupport.class, OnlyIfPresent.class })
final class Target_JsonParserFactory {

	@Substitute
	public static JsonParser getJsonParser() {
		if (ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper", null)) {
			return new JacksonJsonParser();
		}
		if (ClassUtils.isPresent("com.google.gson.Gson", null)) {
			return new GsonJsonParser();
		}
		return new BasicJsonParser();
	}
}
