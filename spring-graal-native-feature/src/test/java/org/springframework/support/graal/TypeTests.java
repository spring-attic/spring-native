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
package org.springframework.support.graal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.TypeInfo;
import org.springframework.graal.type.AccessBits;
import org.springframework.graal.type.CompilationHint;
import org.springframework.graal.type.Type;
import org.springframework.graal.type.TypeSystem;

public class TypeTests {

	static TypeSystem typeSystem;

	@BeforeClass
	public static void setup() throws Exception {
		File file = new File("./target/classes");
		// System.out.println(file.getCanonicalPath());
		typeSystem = new TypeSystem(Collections.singletonList(file.toString()));
	}

	@Test
	public void conversions() {
		String c = Type.fromLdescriptorToSlashed("[Ljava/lang/String;");
		assertEquals("java/lang/String[]", c);
	}

	@Test
	public void simpleResolution() {
		typeSystem.resolveName("java.lang.String");
		typeSystem.resolveName(TestClass1.class.getName());
	}

	@Test
	public void flags() {
		Type testClass = typeSystem.resolveName(TestClass4.class.getName());
		List<CompilationHint> compilationHints = testClass.getCompilationHints();
		assertEquals(1, compilationHints.size());
		CompilationHint ch = compilationHints.get(0);
		assertTrue(ch.isAbortIfTypesMissing());
	}

	@Test
	public void configurationHintConversion() {
		Type testClass = typeSystem.resolveName(TestClass1.class.getName());
		List<CompilationHint> compilationHints = testClass.getCompilationHints();
		assertEquals(1, compilationHints.size());
		CompilationHint ch = compilationHints.get(0);
		assertEquals(Integer.class.getName(), ch.getTargetType());
	}

	@Test
	public void defaultValue() {
		Type testClass = typeSystem.resolveName(TestClass1a.class.getName());
		List<CompilationHint> compilationHints = testClass.getCompilationHints();
		assertEquals(1, compilationHints.size());
		CompilationHint ch = compilationHints.get(0);
		assertEquals(Object.class.getName(), ch.getTargetType());
	}

	@Test
	public void arrayValue() {
		Type testClass = typeSystem.resolveName(TestClass1b.class.getName());
		List<CompilationHint> compilationHints = testClass.getCompilationHints();
		assertEquals(1, compilationHints.size());
		CompilationHint ch = compilationHints.get(0);
		assertEquals(Object.class.getName(), ch.getTargetType());
		assertEquals("java.lang.String[]", ch.getDependantTypes().keySet().iterator().next().toString());
	}

	@Test
	public void configurationHintConversion2() {
		Type testClass = typeSystem.resolveName(TestClass2.class.getName());
		List<CompilationHint> compilationHints = testClass.getCompilationHints();
		assertEquals(1, compilationHints.size());
		CompilationHint ch = compilationHints.get(0);
		assertEquals(Integer.class.getName(), ch.getTargetType());
		Map<String, Integer> dts = ch.getDependantTypes();
		assertEquals(2, dts.size());
		assertEquals((Integer) AccessBits.ALL, dts.get("java.lang.String"));
		assertEquals((Integer) AccessBits.ALL, dts.get("java.lang.Float"));
	}

	@Test
	public void twoHintsOnOneType() {
		Type testClass = typeSystem.resolveName(TestClass3.class.getName());
		List<CompilationHint> compilationHints = testClass.getCompilationHints();
		assertEquals(2, compilationHints.size());
		CompilationHint ch = compilationHints.get(0);
		assertEquals(Integer.class.getName(), ch.getTargetType());
		Map<String, Integer> dts = ch.getDependantTypes();
		assertEquals(1, dts.size());
		assertEquals((Integer) AccessBits.ALL, dts.get("java.lang.String"));
		ch = compilationHints.get(1);
		assertEquals(String.class.getName(), ch.getTargetType());
		dts = ch.getDependantTypes();
		assertEquals(1, dts.size());
		assertEquals((Integer) AccessBits.ALL, dts.get("java.lang.Float"));
	}

	@Test
	public void accessSetOnTypeInfo() {
		Type testClass = typeSystem.resolveName(TestClass5.class.getName());
		List<CompilationHint> compilationHints = testClass.getCompilationHints();
		assertEquals(1, compilationHints.size());
		CompilationHint ch = compilationHints.get(0);
		assertEquals(String.class.getName(), ch.getTargetType());
		Map<String, Integer> dts = ch.getDependantTypes();
		assertEquals(1, dts.size());
		assertEquals((Integer) AccessBits.CLASS, dts.get("java.lang.Float"));
	}

	@Test
	public void doubleTypeInfo() {
		Type testClass = typeSystem.resolveName(TestClass6.class.getName());
		List<CompilationHint> compilationHints = testClass.getCompilationHints();
		assertEquals(1, compilationHints.size());
		CompilationHint ch = compilationHints.get(0);
		assertEquals(String.class.getName(), ch.getTargetType());
		Map<String, Integer> dts = ch.getDependantTypes();
		assertEquals(2, dts.size());
		assertEquals((Integer) AccessBits.CLASS, dts.get("java.lang.Float"));
		assertEquals((Integer) AccessBits.RESOURCE, dts.get("java.lang.Integer"));
	}

	@Test
	public void typeNamesAndTypesInInfo() {
		Type testClass = typeSystem.resolveName(TestClass7.class.getName());
		List<CompilationHint> compilationHints = testClass.getCompilationHints();
		assertEquals(1, compilationHints.size());
		CompilationHint ch = compilationHints.get(0);
		assertEquals(String.class.getName(), ch.getTargetType());
		Map<String, Integer> dts = ch.getDependantTypes();
		assertEquals(2, dts.size());
		assertEquals((Integer) AccessBits.CLASS, dts.get("java.lang.Float"));
		assertEquals((Integer) AccessBits.CLASS, dts.get("java.lang.String"));
	}

	@Test
	public void nestedTypeNameArrays() {
		Type testClass = typeSystem.resolveName(TestClass8.class.getName());
		List<CompilationHint> compilationHints = testClass.getCompilationHints();
		assertEquals(1, compilationHints.size());
		CompilationHint ch = compilationHints.get(0);
		assertEquals(Object.class.getName(), ch.getTargetType());
		Map<String, Integer> dts = ch.getDependantTypes();
		assertEquals(3, dts.size());
		System.out.println(dts);
		// java.lang.String=31
		// org.springframework.support.graal.TypeTests$TestClass6=31, 
		// org.springframework.support.graal.TypeTests$TestClass7[]=31
		Set<String> keys = dts.keySet();
		for (String key: keys) {
			System.out.println("K="+key);
			Type t= typeSystem.resolveName(key);
			assertNotNull(t);
			System.out.println(t.getName());
			System.out.println(t.isArray());
			System.out.println(t.getMethodCount());
		}
	}

	@ConfigurationHint(value = Integer.class)
	static class TestClass1 {
	}

	@ConfigurationHint
	static class TestClass1a {
	}

	@ConfigurationHint(typeInfos = { @TypeInfo(types = { String[].class }) })
	static class TestClass1b {
	}

	@ConfigurationHint(value = Integer.class, typeInfos = { @TypeInfo(types = { String.class, Float.class }) })
	static class TestClass2 {
	}

	@ConfigurationHint(value = Integer.class, typeInfos = { @TypeInfo(types = { String.class }) })
	@ConfigurationHint(value = String.class, typeInfos = { @TypeInfo(types = { Float.class }) })
	static class TestClass3 {
	}

	@ConfigurationHint(value = Integer.class, abortIfTypesMissing = true)
	static class TestClass4 {
	}

	@ConfigurationHint(value = String.class, typeInfos = {
			@TypeInfo(types = { Float.class }, access = AccessBits.CLASS) })
	static class TestClass5 {
	}

	@ConfigurationHint(value = String.class, typeInfos = {
			@TypeInfo(types = { Float.class }, access = AccessBits.CLASS),
			@TypeInfo(types = { Integer.class }, access = AccessBits.RESOURCE) })
	static class TestClass6 {
	}

	@ConfigurationHint(value = String.class, typeInfos = {
			@TypeInfo(typeNames = { "java.lang.String" }, types = { Float.class }, access = AccessBits.CLASS) })
	static class TestClass7 {
	}

	@ConfigurationHint(typeInfos = { @TypeInfo(types = {String[].class},typeNames = { "org.springframework.support.graal.TypeTests$TestClass6",
			"org.springframework.support.graal.TypeTests$TestClass7[]", }) })
	static class TestClass8 {

	}
}
