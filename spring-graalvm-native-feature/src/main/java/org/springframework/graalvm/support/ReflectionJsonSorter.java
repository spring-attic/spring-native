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
package org.springframework.graalvm.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.springframework.graalvm.domain.reflect.ClassDescriptor;
import org.springframework.graalvm.domain.reflect.JsonMarshaller;
import org.springframework.graalvm.domain.reflect.ReflectionDescriptor;

public class ReflectionJsonSorter {

	public static void main(String[] args) {
		if (args==null || args.length!=1) {
			System.out.println("Usage: ReflectionJsonSorter <reflect-config.json>");
			System.exit(1);
		}
		ReflectionDescriptor rd = null;
		try (FileInputStream fis = new FileInputStream(new File(args[0]))) {
			rd = JsonMarshaller.read(fis);
	 	} catch (Exception e) {
	 		throw new IllegalStateException("Problem loading file", e);
	 	}
		List<ClassDescriptor> classDescriptors = rd.getClassDescriptors();
		classDescriptors.sort((a,b) -> a.getName().compareTo(b.getName()));
		rd = new ReflectionDescriptor();
		for (ClassDescriptor cd: classDescriptors) {
			rd.add(cd);
		}
		try {
			JsonMarshaller.write(rd,new PrintStream(System.out));
		} catch (IOException e) {
	 		throw new IllegalStateException("Problem loading file", e);
		}
	}
}
