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

package org.springframework.nativex.domain.init;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.nativex.json.JSONArray;
import org.springframework.nativex.json.JSONObject;

/**
 * Marshaller to write {@link InitializationDescriptor} as JSON.
 *
 * @author Andy Clement
 */
public class InitializationJsonMarshaller {

	private static final int BUFFER_SIZE = 4098;
	
	public static String write(InitializationDescriptor descriptor) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			write(descriptor,baos);
			return baos.toString();
		} catch (IOException ex) {
			throw new IllegalStateException("Unable to write initialization descriptor", ex);
		}
	}

	public static void write(InitializationDescriptor metadata, OutputStream outputStream) {
		try {
			InitializationJsonConverter converter = new InitializationJsonConverter();
			JSONObject jsonObject = converter.toJsonArray(metadata);
			outputStream.write(jsonObject.toString(2).getBytes(StandardCharsets.UTF_8));
		} catch (Exception ex) {
			throw new IllegalStateException("Unable to write initialization descriptor", ex);
		}
	}
	
	public static InitializationDescriptor read(String input) {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))) {
			return read(bais);
		} catch (Exception e) {
			throw new IllegalStateException("Unable to read JSON string", e);
		}
	}

	public static InitializationDescriptor read(byte[] input) throws Exception {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(input)) {
			return read(bais);
		}
	}

	public static InitializationDescriptor read(InputStream inputStream) {
		try {
			return read(new JSONObject(toString(inputStream)));
		} catch (Exception e) {
			throw new IllegalStateException("Unable to read InitialiationDescriptor from inputstream", e);
		}
	}
	
	private static InitializationDescriptor read(JSONObject object) throws Exception {
		InitializationDescriptor rd = new InitializationDescriptor();
		JSONArray array = object.getJSONArray("buildTimeInitialization");
		for (int i=0;i<array.length();i++) {
			JSONObject jsonObject = array.getJSONObject(i);
			if (jsonObject.has("class")) {
				rd.addBuildtimeClass(jsonObject.getString("class"));
			} else if (jsonObject.has("package")) {
				rd.addBuildtimePackage(jsonObject.getString("package"));
			} else {
				throw new IllegalStateException("Unrecognized entry in JSON: "+jsonObject.toString());
			}
		}
		array = object.getJSONArray("runtimeInitialization");
		for (int i=0;i<array.length();i++) {
			JSONObject jsonObject = array.getJSONObject(i);
			if (jsonObject.has("class")) {
				rd.addRuntimeClass(jsonObject.getString("class"));
			} else if (jsonObject.has("package")) {
				rd.addRuntimePackage(jsonObject.getString("package"));
			} else {
				throw new IllegalStateException("Unrecognized entry in JSON: "+jsonObject.toString());
			}
		}
		return rd;
	}

	private static String toString(InputStream inputStream) throws IOException {
		StringBuilder out = new StringBuilder();
		InputStreamReader reader = new InputStreamReader(inputStream,
				StandardCharsets.UTF_8);
		char[] buffer = new char[BUFFER_SIZE];
		int bytesRead;
		while ((bytesRead = reader.read(buffer)) != -1) {
			out.append(buffer, 0, bytesRead);
		}
		return out.toString();
	}

}
