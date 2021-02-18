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
package org.springframework.nativex;

import org.junit.jupiter.api.Test;
import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.domain.reflect.ReflectionDescriptor;
import org.springframework.nativex.domain.resources.ResourcesDescriptor;

public class DescriptorTests {

	@Test
	public void mergeReflectiveDescriptors() {
		ClassDescriptor one = ClassDescriptor.of("one");
		ClassDescriptor two = ClassDescriptor.of("two");
		ReflectionDescriptor a = new ReflectionDescriptor();
		a.add(one);
		System.out.println(a);
		System.out.println("---");
		ReflectionDescriptor b = new ReflectionDescriptor();
		b.add(two);
		System.out.println(b);
		System.out.println("---");
		System.out.println(" merging...");
		a.merge(b);
		System.out.println(a);
	}
	

	@Test
	public void mergeResourceDescriptors() {
		ResourcesDescriptor a = new ResourcesDescriptor();
		a.add("foo/one.json");
		System.out.println(a);
		System.out.println("---");
		ResourcesDescriptor b = new ResourcesDescriptor();
		b.add("bar/two.json");
		System.out.println(b);
		System.out.println("---");
		System.out.println(" merging...");
		a.merge(b);
		System.out.println(a);
	}
	


}
