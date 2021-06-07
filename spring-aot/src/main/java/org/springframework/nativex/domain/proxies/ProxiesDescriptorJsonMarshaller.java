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

package org.springframework.nativex.domain.proxies;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.nativex.json.JSONArray;

/**
 * Marshaller to write {@link ProxiesDescriptor} as JSON.
 *
 * @author Andy Clement
 */
public class ProxiesDescriptorJsonMarshaller {

	private static final int BUFFER_SIZE = 4098;

	public static String write(ProxiesDescriptor descriptor) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			write(descriptor,baos);
			return baos.toString();
		} catch (IOException ex) {
			throw new IllegalStateException("Unable to write proxies descriptor", ex);
		}
	}
	
	public static void write(ProxiesDescriptor metadata, OutputStream outputStream) {
		try {
			ProxiesDescriptorJsonConverter converter = new ProxiesDescriptorJsonConverter();
			JSONArray jsonArray = converter.toJsonArray(metadata);
			outputStream.write(jsonArray.toString(2).getBytes(StandardCharsets.UTF_8));
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}
	
	public static ProxiesDescriptor read(String input) {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))) {
			return read(bais);
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	public static ProxiesDescriptor read(byte[] input) throws Exception {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(input)) {
			return read(bais);
		}
	}

	public static ProxiesDescriptor read(InputStream inputStream) {
		try {
			ProxiesDescriptor metadata = toProxiesDescriptor(new JSONArray(toString(inputStream)));
			return metadata;
		} catch (Exception e) {
			throw new IllegalStateException("Unable to read ProxiesDescriptor from inputstream", e);
		}
	}
	
	private static ProxiesDescriptor toProxiesDescriptor(JSONArray array) throws Exception {
		ProxiesDescriptor pds = new ProxiesDescriptor();
		for (int i=0;i<array.length();i++) {
			pds.add(toProxyDescriptor((JSONArray)array.get(i)));
		}
		return pds;
	}
	
	private static JdkProxyDescriptor toProxyDescriptor(JSONArray array) throws Exception {
		JdkProxyDescriptor pd = new JdkProxyDescriptor();
		List<String> interfaces = new ArrayList<>();
		for (int i=0;i<array.length();i++) {
			interfaces.add(array.getString(i));
		}
		pd.setInterfaces(interfaces);
		return pd;
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
