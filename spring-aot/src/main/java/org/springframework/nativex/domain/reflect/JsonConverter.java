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

package org.springframework.nativex.domain.reflect;

import java.util.List;
import java.util.Set;

import org.springframework.nativex.hint.Flag;
import org.springframework.nativex.json.JSONArray;
import org.springframework.nativex.json.JSONObject;

/**
 * Converter to change {@link ReflectionDescriptor} objects into JSON objects
 *
 * @author Andy Clement
 */
public class JsonConverter {

	public JSONArray toJsonArray(ReflectionDescriptor metadata) throws Exception {
		JSONArray jsonArray = new JSONArray();
		if (metadata.getClassDescriptors() != null) {
			for (ClassDescriptor cd : metadata.getClassDescriptors()) {
				jsonArray.put(toJsonObject(cd));
			}
		}
		return jsonArray;
	}

	public JSONObject toJsonObject(ClassDescriptor cd) throws Exception {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", cd.getName());
		Set<Flag> flags = cd.getFlags();
		if (flags != null) {
			for (Flag flag: Flag.values()) {
				if (flags.contains(flag)) {
					putTrueFlag(jsonObject,flag.name());
				}
			}
		}
		List<FieldDescriptor> fds = cd.getFields();
		if (fds != null) {
			JSONArray fieldJsonArray = new JSONArray();
			for (FieldDescriptor fd: fds) {
				JSONObject fieldjo = new JSONObject();
				fieldjo.put("name", fd.getName());
				if (fd.isAllowWrite()) {
					fieldjo.put("allowWrite", true);
				}
				if (fd.isAllowUnsafeAccess()) {
					fieldjo.put("allowUnsafeAccess", true);
				}
				fieldJsonArray.put(fieldjo);
			}
			jsonObject.put("fields", fieldJsonArray);
		}
		List<MethodDescriptor> mds = cd.getMethods();
		if (mds != null) {
			JSONArray methodsJsonArray = new JSONArray();
			for (MethodDescriptor md: mds) {
				JSONObject methodJsonObject = new JSONObject();
				methodJsonObject.put("name", md.getName());
				List<String> parameterTypes = md.getParameterTypes();
					JSONArray parameterArray = new JSONArray();
				if (parameterTypes != null) {
					for (String pt: parameterTypes) {
						parameterArray.put(pt);
					}
				}
					methodJsonObject.put("parameterTypes",parameterArray);
				methodsJsonArray.put(methodJsonObject);
			}
			jsonObject.put("methods", methodsJsonArray);
		}
		return jsonObject;
	}

	private void putTrueFlag(JSONObject jsonObject, String name) throws Exception {
		jsonObject.put(name, true);
	}
}
