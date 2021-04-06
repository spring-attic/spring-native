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
 * Marshaller to write {@link ResourcesDescriptor} as JSON.
 *
 * @author Andy Clement
 */
public class ResourcesJsonMarshaller {

	private static final int BUFFER_SIZE = 4098;

	public static String write(ResourcesDescriptor descriptor) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			write(descriptor,baos);
			return baos.toString();
		} catch (IOException ex) {
			throw new IllegalStateException("Unable to write resources descriptor", ex);
		}
	}

	public static void write(ResourcesDescriptor metadata, OutputStream outputStream) {
		try {
			ResourcesJsonConverter converter = new ResourcesJsonConverter();
			JSONObject jsonObject = converter.toJsonArray(metadata);
			outputStream.write(jsonObject.toString(2)
					.replace("\\/","/")
					.getBytes(StandardCharsets.UTF_8));
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	public static ResourcesDescriptor read(String input) {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))) {
			return read(bais);
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	public static ResourcesDescriptor read(byte[] input) throws Exception {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(input)) {
			return read(bais);
		}
	}

	public static ResourcesDescriptor read(InputStream inputStream) {
		try {
			ResourcesDescriptor metadata = toResourcesDescriptor(new JSONObject(toString(inputStream)));
			return metadata;
		} catch (Exception e) {
			throw new IllegalStateException("Unable to read ResourcesDescriptor from inputstream", e);
		}
	}

	private static ResourcesDescriptor toResourcesDescriptor(JSONObject object) throws Exception {
		ResourcesDescriptor rd = new ResourcesDescriptor();
		JSONArray array = null;
		if (object.has("bundles")) {
			array = object.getJSONArray("bundles");
			for (int i=0;i<array.length();i++) {
				rd.addBundle(array.getJSONObject(i).getString("name"));
			}
		}
		if (object.has("resources")) {
			try {
				array = object.getJSONArray("resources");
			} catch (Exception ex) {
				// Support for GraalVM 20.3 format introduced by https://github.com/oracle/graal/commit/5b0a7453bcfb09918d8f615e64bf0430c8abbbfc#diff-14b5d0463c9666d011e03cc880ba3ca4a1da754f51e8fd8af5dabbfc6fd64476
				array = object.getJSONObject("resources").getJSONArray("includes");
			}
			for (int i=0;i<array.length();i++) {
				rd.add(array.getJSONObject(i).getString("pattern"));
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
