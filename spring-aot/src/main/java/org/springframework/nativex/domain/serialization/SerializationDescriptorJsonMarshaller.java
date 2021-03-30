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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.nativex.json.JSONArray;
import org.springframework.nativex.json.JSONObject;

/**
 * Marshaller to write {@link SerializationDescriptor} as JSON.
 *
 * @author Andy Clement
 */
public class SerializationDescriptorJsonMarshaller {

	private static final int BUFFER_SIZE = 4098;

	public static void write(SerializationDescriptor descriptor, OutputStream outputStream)
			throws IOException {
		try {
			SerializationDescriptorJsonConverter converter = new SerializationDescriptorJsonConverter();
			JSONArray jsonArray = converter.toJsonArray(descriptor);
			outputStream.write(jsonArray.toString(2).getBytes(StandardCharsets.UTF_8));
		}
		catch (Exception ex) {
			if (ex instanceof IOException) {
				throw (IOException) ex;
			}
			if (ex instanceof RuntimeException) {
				throw (RuntimeException) ex;
			}
			throw new IllegalStateException(ex);
		}
	}
	
	public static SerializationDescriptor read(String input) throws Exception {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))) {
			return read(bais);
		}
	}

	public static SerializationDescriptor read(byte[] input) throws Exception {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(input)) {
			return read(bais);
		}
	}

	public static SerializationDescriptor read(InputStream inputStream) {
		try {
			SerializationDescriptor descriptor = toSerializationDescriptor(new JSONArray(toString(inputStream)));
			return descriptor;
		} catch (Exception e) {
			throw new IllegalStateException("Unable to read ProxiesDescriptor from inputstream", e);
		}
	}
	
	private static SerializationDescriptor toSerializationDescriptor(JSONArray array) throws Exception {
		SerializationDescriptor descriptor = new SerializationDescriptor();
		for (int i=0;i<array.length();i++) {
			JSONObject object = (JSONObject) array.get(i);
			descriptor.add(object.getString("name"));
		}
		return descriptor;
	}
	
	// TODO move to common utility - how many classes are duplicating this?
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
