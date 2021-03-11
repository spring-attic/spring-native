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

package org.springframework.nativex.domain.resources;

import org.springframework.nativex.json.JSONArray;
import org.springframework.nativex.json.JSONObject;

/**
 * Converter to change {@link ResourcesDescriptor} objects into JSON objects.
 *
 * @author Andy Clement
 */
class ResourcesJsonConverter {

	public JSONObject toJsonArray(ResourcesDescriptor metadata) throws Exception {
		JSONObject object = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		if (metadata.getPatterns() != null) {
			for (String p : metadata.getPatterns()) {
				jsonArray.put(toPatternJsonObject(p));
			}
		}
		JSONObject includes = new JSONObject();
		includes.put("includes", jsonArray);
		object.put("resources", includes);
		JSONArray bundleArray = null;
		if (metadata.getBundles() != null) {
			bundleArray = new JSONArray();
			for (String b: metadata.getBundles()) {
				bundleArray.put(toBundleJsonObject(b));
			}
			object.put("bundles", bundleArray);
		}
		return object;
	}
	
	public JSONObject toBundleJsonObject(String bundle) throws Exception {
		JSONObject bundleObject = new JSONObject();
		bundleObject.put("name", bundle);
		return bundleObject;
	}

	public JSONObject toPatternJsonObject(String pattern) throws Exception {
		JSONObject object = new JSONObject();
		object.put("pattern", pattern);
		return object;
	}

}
