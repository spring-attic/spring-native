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
