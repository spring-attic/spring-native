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
import java.util.List;

import org.springframework.graalvm.domain.reflect.ClassDescriptor;
import org.springframework.graalvm.domain.reflect.JsonMarshaller;
import org.springframework.graalvm.domain.reflect.ReflectionDescriptor;

public class ReflectionJsonComparator {

	public static void main(String[] args) {
		if (args==null || args.length!=2) {
			System.out.println("Usage: ReflectionJsonComparator <reflect-config1.json> <reflect-config2.json>");
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
		
		int firstNotInSecond = 0;
		int secondNotInFirst = 0;
		int sameInBoth = 0;
		int differentInBoth = 0;
		System.out.println("These are in the first reflection file but not in the second:");
		for (ClassDescriptor cd1: cds1) {
			if (getClassDescriptor(cds2, cd1.getName())==null) {
				System.out.println("< "+cd1.toString());
			firstNotInSecond++;
			}
		}
		
		System.out.println("These are in the second reflection file but not in the first:");
		for (ClassDescriptor cd2: cds2) {
			if (getClassDescriptor(cds1, cd2.getName())==null) {
				System.out.println("> "+cd2.toString());
			secondNotInFirst++;
			}
		}
		
		System.out.println("These are in both files and configured the same:");
		for (ClassDescriptor cd1: cds1) {
			ClassDescriptor cd2 = getClassDescriptor(cds2,cd1.getName());
			if (cd2 != null) {
				if (cd1.equals(cd2)) {
					System.out.println("= "+cd1);	
			sameInBoth++;
				}
			}
		}
		System.out.println("These are in both files but configured differently in each:");
		for (ClassDescriptor cd1: cds1) {
			ClassDescriptor cd2 = getClassDescriptor(cds2,cd1.getName());
			if (cd2 != null) {
				if (!cd1.equals(cd2)) {
					System.out.println("1?"+cd1);
					System.out.println("2?"+cd2);
			differentInBoth++;
				}
			}
		}
		System.out.println("Summary:");
		System.out.println("In first but not second: "+firstNotInSecond);
		System.out.println("In second but not first: "+secondNotInFirst);
		System.out.println("In both files but configured differently: "+differentInBoth);
		System.out.println("In both files and configured the same: "+sameInBoth);
	}

	private static ClassDescriptor getClassDescriptor(List<ClassDescriptor> cds, String name) {
		for (ClassDescriptor cd: cds) {
			if (cd.getName().equals(name)) {
				return cd;
			}
		}
		return null;
	}
}
