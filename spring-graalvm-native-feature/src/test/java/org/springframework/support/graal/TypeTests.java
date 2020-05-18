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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;
import org.springframework.graalvm.type.CompilationHint;
import org.springframework.graalvm.type.Type;
import org.springframework.graalvm.type.TypeSystem;
import org.springframework.transaction.annotation.Transactional;

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
			Type t= typeSystem.resolveName(key);
			assertNotNull(t);
			System.out.println(t.getName());
			System.out.println(t.isArray());
			System.out.println(t.getMethodCount());
		}
	}
	
	@Test
	public void isTransactional() {
		// TODO verify how to deal with a type extending a type that has transactional methods - are instances
		// of the subtype considered transactional?
		Type txClass1 = typeSystem.resolveName(TXClass1.class.getName());
		assertTrue(txClass1.isTransactional());
		Type txClass2 = typeSystem.resolveName(TXClass2.class.getName());
		assertFalse(txClass2.isTransactional());
		assertTrue(txClass2.hasTransactionalMethods());
		Type tClass1 = typeSystem.resolveName(TestClass1.class.getName());
		assertFalse(tClass1.isTransactional());
		Type txClass3 = typeSystem.resolveName(TXClass3.class.getName());
		assertFalse(txClass3.isTransactional());
		Type txClass4 = typeSystem.resolveName(TXClass4.class.getName());
		assertFalse(txClass4.isTransactional());
	}
	
	@Test
	public void responseBody() {
		Type testController = typeSystem.resolveName(TestController1.class.getName());
		assertTrue(testController.hasAnnotation("L"+ResponseBody.class.getName().replace(".","/")+";", true));
		Type testController2 = typeSystem.resolveName(TestController2.class.getName());
		assertTrue(testController2.hasAnnotation("L"+ResponseBody.class.getName().replace(".","/")+";", true));
	}
	
	@Test
	public void findReactiveCrudTypeParameter() {
		Type t = typeSystem.resolveName(Foo.class.getName());
		String s = t.findTypeParameterInSupertype(FooI.class.getName(),0);
		assertEquals(TestController1.class.getName(),s);
	}
	
	@Test
	public void typeParameters() {
		Type extenderClass = typeSystem.resolveName(Extender.class.getName());
		List<String> typesInSignature = extenderClass.getTypesInSignature();
		assertEquals(3, typesInSignature.size());
		assertEquals("java/lang/Object",typesInSignature.get(0));
		assertEquals("org/springframework/support/graal/TypeTests$Finder",typesInSignature.get(1));
		assertEquals("org/springframework/support/graal/TypeTests$TXClass4",typesInSignature.get(2));
		Type extender2 = typeSystem.resolveName(Extender2.class.getName());
		typesInSignature = extender2.getTypesInSignature();
		assertEquals(2, typesInSignature.size());
		assertEquals("org/springframework/support/graal/TypeTests$TXClass4",typesInSignature.get(0));
		assertEquals("java/io/Serializable",typesInSignature.get(1));
	}
	
	@SuppressWarnings("serial")
	static class Extender2 extends TXClass4 implements Serializable {
	}

	static class Extender implements Finder<TXClass4> {
	}

	interface Finder<T> {
	}

	static class TXClass4 extends TXClass1 {
	}
	
	static class TXClass3 extends TXClass2 {
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@interface ResponseBody {}

	@Retention(RetentionPolicy.RUNTIME)
	@ResponseBody
	@interface RestController {}
	
	interface FooI<T,ID> { }
	
	static class Foo implements FooI<TestController1,Integer> {}
	
	@ResponseBody
	static class TestController1 {}

	@RestController // Meta annotated with ResponseBody
	static class TestController2 {}

	@Transactional
	static class TXClass1 {
	}

	static class TXClass2 {
		@Transactional
		public void m() {}
	}

	@NativeImageHint(trigger = Integer.class)
	static class TestClass1 {
	}

	@NativeImageHint
	static class TestClass1a {
	}

	@NativeImageHint(typeInfos = { @TypeInfo(types = { String[].class }) })
	static class TestClass1b {
	}

	@NativeImageHint(trigger = Integer.class, typeInfos = { @TypeInfo(types = { String.class, Float.class }) })
	static class TestClass2 {
	}

	@NativeImageHint(trigger = Integer.class, typeInfos = { @TypeInfo(types = { String.class }) })
	@NativeImageHint(trigger = String.class, typeInfos = { @TypeInfo(types = { Float.class }) })
	static class TestClass3 {
	}

	@NativeImageHint(trigger = Integer.class, abortIfTypesMissing = true)
	static class TestClass4 {
	}

	@NativeImageHint(trigger = String.class, typeInfos = {
			@TypeInfo(types = { Float.class }, access = AccessBits.CLASS) })
	static class TestClass5 {
	}

	@NativeImageHint(trigger = String.class, typeInfos = {
			@TypeInfo(types = { Float.class }, access = AccessBits.CLASS),
			@TypeInfo(types = { Integer.class }, access = AccessBits.RESOURCE) })
	static class TestClass6 {
	}

	@NativeImageHint(trigger = String.class, typeInfos = {
			@TypeInfo(typeNames = { "java.lang.String" }, types = { Float.class }, access = AccessBits.CLASS) })
	static class TestClass7 {
	}

	@NativeImageHint(typeInfos = { @TypeInfo(types = {String[].class},typeNames = { "org.springframework.support.graal.TypeTests$TestClass6",
			"org.springframework.support.graal.TypeTests$TestClass7[]", }) })
	static class TestClass8 {

	}
}
