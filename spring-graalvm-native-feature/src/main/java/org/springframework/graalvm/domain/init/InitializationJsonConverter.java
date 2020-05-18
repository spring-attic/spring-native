/*
 * Copyright 2019 the original author or authors.
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

package org.springframework.graalvm.domain.init;

import org.springframework.graalvm.json.JSONArray;
import org.springframework.graalvm.json.JSONObject;

/**
 * Converter to change resource descriptor objects into JSON objects
 *
 * @author Andy Clement
 */
class InitializationJsonConverter {

	public JSONObject toJsonArray(InitializationDescriptor metadata) throws Exception {
		JSONObject object = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		for (String p : metadata.getBuildtimeClasses()) {
			jsonArray.put(toClassJsonObject(p));
		}
		for (String p : metadata.getBuildtimePackages()) {
			jsonArray.put(toPackageJsonObject(p));
		}
		object.put("buildTimeInitialization", jsonArray);
		for (String p : metadata.getRuntimeClasses()) {
			jsonArray.put(toClassJsonObject(p));
		}
		for (String p : metadata.getRuntimePackages()) {
			jsonArray.put(toPackageJsonObject(p));
		}
		object.put("runtimeInitialization", jsonArray);
		return object;
	}

	public JSONObject toPackageJsonObject(String pattern) throws Exception {
		JSONObject object = new JSONObject();
		object.put("package", pattern);
		return object;
	}
	
	public JSONObject toClassJsonObject(String pattern) throws Exception {
		JSONObject object = new JSONObject();
		object.put("class", pattern);
		return object;
	}
}
