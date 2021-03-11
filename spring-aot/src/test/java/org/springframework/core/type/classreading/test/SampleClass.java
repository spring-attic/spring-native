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

package org.springframework.core.type.classreading.test;

import org.springframework.core.annotation.Order;

public class SampleClass {

	static {
		String test = "";
	}

	public SampleClass() {

	}

	public SampleClass(String name) {

	}

	public String hello(String name) {
		return "Hello";
	}

	@Order(42)
	public String annotated() {
		return "test";
	}

	public interface InnerClass {

		void someMethod();

	}

}
