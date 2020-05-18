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
package org.springframework.graalvm.domain.reflect;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.graalvm.json.JSONArray;
import org.springframework.graalvm.json.JSONObject;

/**
 * Marshaller to write {@link ReflectionDescriptor} as JSON.
 *
 * @author Andy Clement
 */
public class JsonMarshaller {

	private static final int BUFFER_SIZE = 4098;

	public static void write(ReflectionDescriptor metadata, OutputStream outputStream)
			throws IOException {
		try {
			JsonConverter converter = new JsonConverter();
			JSONArray jsonArray = converter.toJsonArray(metadata);
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
	
	public static ReflectionDescriptor read(String input) throws Exception {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))) {
			return read(bais);
		}
	}

	public static ReflectionDescriptor read(byte[] input) throws Exception {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(input)) {
			return read(bais);
		}
	}

	public static ReflectionDescriptor read(InputStream inputStream) throws Exception {
		ReflectionDescriptor metadata = toReflectionDescriptor(new JSONArray(toString(inputStream)));
		return metadata;
	}
	
	private static ReflectionDescriptor toReflectionDescriptor(JSONArray array) throws Exception {
		ReflectionDescriptor rd = new ReflectionDescriptor();
		for (int i=0;i<array.length();i++) {
			ClassDescriptor cd = toClassDescriptor((JSONObject)array.get(i));
			if (rd.hasClassDescriptor(cd.getName())) {
				System.out.println("DUPLICATE: "+cd.getName());
				rd.getClassDescriptor(cd.getName()).merge(cd);
			} else {
				rd.add(cd);
			}
		}
		return rd;
	}
	
	private static ClassDescriptor toClassDescriptor(JSONObject object) throws Exception {
		ClassDescriptor cd = new ClassDescriptor();
		cd.setName(object.getString("name"));
		for (Flag f: Flag.values()) {
			if (object.optBoolean(f.name())) {
				cd.setFlag(f);
			}
		}
		JSONArray fields = object.optJSONArray("fields");
		if (fields != null) {
			for (int i=0;i<fields.length();i++) {
				cd.addFieldDescriptor(toFieldDescriptor(fields.getJSONObject(i)));
			}
		}
		JSONArray methods = object.optJSONArray("methods");
		if (methods != null) {
			for (int i=0;i<methods.length();i++) {
				cd.addMethodDescriptor(toMethodDescriptor(methods.getJSONObject(i)));
			}
		}
		return cd;
	}
	
	private static FieldDescriptor toFieldDescriptor(JSONObject object) throws Exception {
		String name = object.getString("name");
		boolean allowWrite = object.optBoolean("allowWrite");
		boolean allowUnsafeAccess = object.optBoolean("allowUnsafeAccess"); // Need to confirm this is right
		return new FieldDescriptor(name,allowWrite,allowUnsafeAccess);
	}

	private static MethodDescriptor toMethodDescriptor(JSONObject object) throws Exception {
		String name = object.getString("name");
		JSONArray parameterTypes = object.optJSONArray("parameterTypes");
		List<String> listOfParameterTypes = null;
		if (parameterTypes != null) {
			listOfParameterTypes = new ArrayList<>();
			for (int i=0;i<parameterTypes.length();i++) {
				listOfParameterTypes.add(parameterTypes.getString(i));
			}
		}
		return new MethodDescriptor(name, listOfParameterTypes);
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
