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

package org.springframework.nativex.domain.serialization;

import org.springframework.nativex.json.JSONArray;
import org.springframework.nativex.json.JSONObject;
import org.springframework.nativex.json.JSONValue;

/**
 * Converter to change {@link SerializationDescriptor} objects into JSON objects
 *
 * @author Andy Clement
 */
class SerializationDescriptorJsonConverter {

	public JSONValue toJsonValue(SerializationDescriptor sd) throws Exception {
		if (sd.getSerializableLambdaCapturingTypes().isEmpty()) {
			JSONArray jsonArray = new JSONArray();
			for (String type: sd.getSerializableTypes()) {
				JSONObject jo = new JSONObject();
				jo.put("name", type);
				jsonArray.put(jo);
			}
			return jsonArray;
		}
		JSONObject jsonObject = new JSONObject();
		JSONArray types = new JSONArray();
		for (String type: sd.getSerializableTypes()) {
			JSONObject jo = new JSONObject();
			jo.put("name", type);
			types.put(jo);
		}
		jsonObject.put("types", types);
		JSONArray lambdaCapturingTypes = new JSONArray();
		for (String type: sd.getSerializableLambdaCapturingTypes()) {
			JSONObject jo = new JSONObject();
			jo.put("name", type);
			lambdaCapturingTypes.put(jo);
		}
		jsonObject.put("lambdaCapturingTypes", lambdaCapturingTypes);
		return jsonObject;
	}

}
