/*
 * Copyright 2020 the original author or authors.
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
package org.springframework.graalvm.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import org.springframework.graalvm.domain.reflect.ClassDescriptor;
import org.springframework.graalvm.domain.reflect.JsonConverter;
import org.springframework.graalvm.domain.reflect.JsonMarshaller;
import org.springframework.graalvm.domain.reflect.ReflectionDescriptor;

/**
 * This will take an input reflect-config.json and a target reflect-config.json.
 * It will remove entries from the target that are in the source. This can be
 * used to remove entries from an agent generated list that are computed by the
 * feature.
 * 
 * @author Andy Clement
 */
public class ReflectionJsonStrip {

	public static void main(String[] args) throws Exception {
		if (args == null || args.length != 2) {
			System.out.println("Usage: ReflectionJsonStrip <input-reflect-config.json> <target-reflect-config.json>");
			System.exit(1);
		}
		ReflectionDescriptor rd1 = null;
		try (FileInputStream fis = new FileInputStream(new File(args[0]))) {
			rd1 = JsonMarshaller.read(fis);
		} catch (Exception e) {
			throw new IllegalStateException("Problem loading file 1", e);
		}
		ReflectionDescriptor rd2 = null;
		try (FileInputStream fis = new FileInputStream(new File(args[1]))) {
			rd2 = JsonMarshaller.read(fis);
		} catch (Exception e) {
			throw new IllegalStateException("Problem loading file 2", e);
		}
		List<ClassDescriptor> cds1 = rd1.getClassDescriptors();
		List<ClassDescriptor> cds2 = rd2.getClassDescriptors();

		File newStrippedFile = new File(args[1] + ".stripped");
		StringBuilder json = new StringBuilder();
		json.append("[\n");
		for (ClassDescriptor cd2 : cds2) {
			if (getClassDescriptor(cds1, cd2.getName()) == null) {
				System.out.println("Did not find "+cd2.getName()+" in the first file, including it in output");
				// This one is not mentioned in the source, so we must include it in output
				json.append(new JsonConverter().toJsonObject(cd2).toString() + ",\n");
			} else {
				/*
				// In both, but are they different?
				ClassDescriptor cd1 = getClassDescriptor(cds1, cd2.getName());
				if (!cd1.equals(cd2)) {
					// These differ, go with the agent one for now
					System.out.println("These differ: "+cd1.getName());
					System.out.println(cd1.toString());
					System.out.println(cd2.toString());
					json.append(new JsonConverter().toJsonObject(cd2) + ",\n");
				}
				*/
			}
		}
		json.setCharAt(json.length() - 2, ' '); // splat over last ,
		json.append("\n]");
		String output = json.toString();
		try (FileOutputStream fos = new FileOutputStream(newStrippedFile)) {
			fos.write(output.getBytes());
		}
	}

	private static ClassDescriptor getClassDescriptor(List<ClassDescriptor> cds, String name) {
		for (ClassDescriptor cd : cds) {
			if (cd.getName().equals(name)) {
				return cd;
			}
		}
		return null;
	}
}
