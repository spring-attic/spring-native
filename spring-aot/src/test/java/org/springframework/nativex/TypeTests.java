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

package org.springframework.nativex;

import java.io.File;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.nativex.domain.reflect.MethodDescriptor;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.SerializationHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.AccessDescriptor;
import org.springframework.nativex.type.HintDeclaration;
import org.springframework.nativex.type.Field;
import org.springframework.nativex.type.Method;
import org.springframework.nativex.type.Type;
import org.springframework.nativex.type.TypeSystem;
//import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TypeTests {

	static TypeSystem typeSystem;

	@BeforeAll
	public static void setup() throws Exception {
		File file = new File("./target/test-classes");
		typeSystem = new TypeSystem(Collections.singletonList(file.toString()));
	}
	
	@Test
	public void subtypesClasses() {
		Type resolve = typeSystem.resolve(AA.class);
		List<Type> subtypes = resolve.getSubtypes();
		assertThat(subtypes.size()).isEqualTo(2);
		assertThat(subtypes).contains(typeSystem.resolve(BB.class));
		assertThat(subtypes).contains(typeSystem.resolve(CC.class));
	}

	static class AA {
	}
	
	static class BB extends AA {
	}
	
	static class CC extends BB {	
	}

	@Test
	public void subtypesInterfaces() {
		Type resolve = typeSystem.resolve(III.class);
		List<Type> subtypes = resolve.getSubtypes();
		assertThat(subtypes.size()).isEqualTo(3);
		assertThat(subtypes).contains(typeSystem.resolve(AAA.class));
		assertThat(subtypes).contains(typeSystem.resolve(BBB.class));
		assertThat(subtypes).contains(typeSystem.resolve(DDD.class));
	}

	static interface III {
	}
	
	static class AAA implements III {	
	}
	
	static class BBB extends AAA {
	}
	
	static interface DDD extends III {	
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
	public void serializationHints() {
		Type s = typeSystem.resolveName(Ser1.class.getName());
		List<HintDeclaration> hds = s.getCompilationHints();
		assertEquals(1, hds.size());
		HintDeclaration hd = hds.get(0);
		Set<String> serializationTypes = hd.getSerializationTypes();
		assertEquals(1, serializationTypes.size());
		assertTrue(serializationTypes.contains("java.lang.String"));
	}

	@Test
	public void serializationHints2() {
		Type s = typeSystem.resolveName(Ser2.class.getName());
		List<HintDeclaration> hds = s.getCompilationHints();
		assertEquals(1, hds.size());
		HintDeclaration hd = hds.get(0);
		assertEquals("java.lang.String",hd.getTriggerTypename());
		Set<String> serializationTypes = hd.getSerializationTypes();
		assertEquals(2, serializationTypes.size());
		assertTrue(serializationTypes.contains("java.lang.String"));
		assertTrue(serializationTypes.contains("java.lang.Integer"));
	}

	@Test
	public void serializationHints3() {
		Type s = typeSystem.resolveName(Ser3.class.getName());
		List<HintDeclaration> hds = s.getCompilationHints();
		assertEquals(1, hds.size());
		HintDeclaration hd = hds.get(0);
		assertEquals("java.lang.String",hd.getTriggerTypename());
		Set<String> serializationTypes = hd.getSerializationTypes();
		assertEquals(2, serializationTypes.size());
		assertTrue(serializationTypes.contains("java.lang.String"));
		assertTrue(serializationTypes.contains("java.lang.Integer"));
	}
	
	@SerializationHint(types = String.class)
	static class Ser1 {
	}
	
	@NativeHint(trigger = String.class, serializables = @SerializationHint(types=String.class, typeNames = "java.lang.Integer"))
	static class Ser2 {
	}

	@NativeHint(trigger = String.class, imports = SerializationImports.class)
	static class Ser3 {
	}

	@SerializationHint(types=String.class, typeNames = "java.lang.Integer")
	static class SerializationImports {
	}
	
	@Test
	public void jniTypeHints() {
		Type s = typeSystem.resolveName(Jni1.class.getName());
		List<HintDeclaration> hds = s.getCompilationHints();
		assertEquals(1, hds.size());
		HintDeclaration hd = hds.get(0);
		Map<String, AccessDescriptor> jniTypes = hd.getJNITypes();
		assertEquals(1, jniTypes.size());
		assertTrue(jniTypes.containsKey("java.lang.String"));
		assertEquals(AccessBits.LOAD_AND_CONSTRUCT, jniTypes.get("java.lang.String").getAccessBits());
	}
	
	@Test
	public void jniTypeHints2() {
		Type s = typeSystem.resolveName(Jni2.class.getName());
		List<HintDeclaration> hds = s.getCompilationHints();
		assertEquals(1, hds.size());
		HintDeclaration hd = hds.get(0);
		assertEquals("java.lang.Integer", hd.getTriggerTypename());
		Map<String, AccessDescriptor> jniTypes = hd.getJNITypes();
		assertEquals(1, jniTypes.size());
		assertTrue(jniTypes.containsKey("java.lang.Boolean"));
		assertEquals(AccessBits.LOAD_AND_CONSTRUCT, jniTypes.get("java.lang.Boolean").getAccessBits());
	}

	@TypeHint(types = String.class, access=AccessBits.LOAD_AND_CONSTRUCT|AccessBits.JNI)
	static class Jni1 {
	}

	@NativeHint(trigger=Integer.class,types = 
			@TypeHint(types = Boolean.class, access=AccessBits.LOAD_AND_CONSTRUCT|AccessBits.JNI)
	)
	static class Jni2 {
	}

	@Test
	public void flags() {
		Type testClass = typeSystem.resolveName(TestClass4.class.getName());
		List<HintDeclaration> compilationHints = testClass.getCompilationHints();
		assertEquals(1, compilationHints.size());
		HintDeclaration ch = compilationHints.get(0);
		assertTrue(ch.isAbortIfTypesMissing());
	}

	@Test
	public void configurationHintConversion() {
		Type testClass = typeSystem.resolveName(TestClass1.class.getName());
		List<HintDeclaration> compilationHints = testClass.getCompilationHints();
		assertEquals(1, compilationHints.size());
		HintDeclaration ch = compilationHints.get(0);
		assertEquals(Integer.class.getName(), ch.getTriggerTypename());
	}

	@Test
	public void defaultValue() {
		Type testClass = typeSystem.resolveName(TestClass1a.class.getName());
		List<HintDeclaration> compilationHints = testClass.getCompilationHints();
		assertEquals(1, compilationHints.size());
		HintDeclaration ch = compilationHints.get(0);
		assertEquals(Object.class.getName(), ch.getTriggerTypename());
	}

	@Test
	public void arrayValue() {
		Type testClass = typeSystem.resolveName(TestClass1b.class.getName());
		List<HintDeclaration> compilationHints = testClass.getCompilationHints();
		assertEquals(1, compilationHints.size());
		HintDeclaration ch = compilationHints.get(0);
		assertEquals(Object.class.getName(), ch.getTriggerTypename());
		assertEquals("java.lang.String[]", ch.getDependantTypes().keySet().iterator().next().toString());
	}

	@Test
	public void configurationHintConversion2() {
		Type testClass = typeSystem.resolveName(TestClass2.class.getName());
		List<HintDeclaration> compilationHints = testClass.getCompilationHints();
		assertEquals(1, compilationHints.size());
		HintDeclaration ch = compilationHints.get(0);
		assertEquals(Integer.class.getName(), ch.getTriggerTypename());
		Map<String, AccessDescriptor> dts = ch.getDependantTypes();
		assertEquals(2, dts.size());
		assertEquals((Integer) AccessBits.FULL_REFLECTION, dts.get("java.lang.String").getAccessBits());
		assertEquals((Integer) AccessBits.FULL_REFLECTION, dts.get("java.lang.Float").getAccessBits());
	}

	@Test
	public void twoHintsOnOneType() {
		Type testClass = typeSystem.resolveName(TestClass3.class.getName());
		List<HintDeclaration> compilationHints = testClass.getCompilationHints();
		assertEquals(2, compilationHints.size());
		HintDeclaration ch = compilationHints.get(0);
		assertEquals(Integer.class.getName(), ch.getTriggerTypename());
		Map<String, AccessDescriptor> dts = ch.getDependantTypes();
		assertEquals(1, dts.size());
		assertEquals((Integer) AccessBits.FULL_REFLECTION, dts.get("java.lang.String").getAccessBits());
		ch = compilationHints.get(1);
		assertEquals(String.class.getName(), ch.getTriggerTypename());
		dts = ch.getDependantTypes();
		assertEquals(1, dts.size());
		assertEquals((Integer) AccessBits.FULL_REFLECTION, dts.get("java.lang.Float").getAccessBits());
	}

	@Test
	public void accessSetOnTypeInfo() {
		Type testClass = typeSystem.resolveName(TestClass5.class.getName());
		List<HintDeclaration> compilationHints = testClass.getCompilationHints();
		assertEquals(1, compilationHints.size());
		HintDeclaration ch = compilationHints.get(0);
		assertEquals(String.class.getName(), ch.getTriggerTypename());
		Map<String, AccessDescriptor> dts = ch.getDependantTypes();
		assertEquals(1, dts.size());
		assertEquals((Integer) AccessBits.CLASS, dts.get("java.lang.Float").getAccessBits());
	}

	@Test
	public void doubleTypeInfo() {
		Type testClass = typeSystem.resolveName(TestClass6.class.getName());
		List<HintDeclaration> compilationHints = testClass.getCompilationHints();
		assertEquals(1, compilationHints.size());
		HintDeclaration ch = compilationHints.get(0);
		assertEquals(String.class.getName(), ch.getTriggerTypename());
		Map<String, AccessDescriptor> dts = ch.getDependantTypes();
		assertEquals(2, dts.size());
		assertEquals((Integer) AccessBits.CLASS, dts.get("java.lang.Float").getAccessBits());
		assertEquals((Integer) AccessBits.RESOURCE, dts.get("java.lang.Integer").getAccessBits());
	}

	@Test
	public void typeNamesAndTypesInInfo() {
		Type testClass = typeSystem.resolveName(TestClass7.class.getName());
		List<HintDeclaration> compilationHints = testClass.getCompilationHints();
		assertEquals(1, compilationHints.size());
		HintDeclaration ch = compilationHints.get(0);
		assertEquals(String.class.getName(), ch.getTriggerTypename());
		Map<String, AccessDescriptor> dts = ch.getDependantTypes();
		assertEquals(2, dts.size());
		assertEquals((Integer) AccessBits.CLASS, dts.get("java.lang.Float").getAccessBits());
		assertEquals((Integer) AccessBits.CLASS, dts.get("java.lang.String").getAccessBits());
	}

	@Test
	public void nestedTypeNameArrays() {
		Type testClass = typeSystem.resolveName(TestClass8.class.getName());
		List<HintDeclaration> compilationHints = testClass.getCompilationHints();
		assertEquals(1, compilationHints.size());
		HintDeclaration ch = compilationHints.get(0);
		assertEquals(Object.class.getName(), ch.getTriggerTypename());
		Map<String, AccessDescriptor> dts = ch.getDependantTypes();
		assertEquals(3, dts.size());
		Set<String> keys = dts.keySet();
		for (String key: keys) {
			Type t= typeSystem.resolveName(key);
			assertNotNull(t);
		}
	}
	
//	@Test
//	public void isTransactional() {
//		// TODO verify how to deal with a type extending a type that has transactional methods - are instances
//		// of the subtype considered transactional?
//		Type txClass1 = typeSystem.resolveName(TXClass1.class.getName());
//		assertTrue(txClass1.isTransactional());
//		Type txClass2 = typeSystem.resolveName(TXClass2.class.getName());
//		assertFalse(txClass2.isTransactional());
//		assertTrue(txClass2.hasTransactionalMethods());
//		Type tClass1 = typeSystem.resolveName(TestClass1.class.getName());
//		assertFalse(tClass1.isTransactional());
//		Type txClass3 = typeSystem.resolveName(TXClass3.class.getName());
//		assertFalse(txClass3.isTransactional());
//		Type txClass4 = typeSystem.resolveName(TXClass4.class.getName());
//		assertFalse(txClass4.isTransactional());
//	}
	
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
		Set<String> typesInSignature = extenderClass.getTypesInSignature();
		assertEquals(3, typesInSignature.size());
		Iterator<String> iterator = typesInSignature.iterator();
		assertEquals("java/lang/Object",iterator.next());
		assertEquals("org/springframework/nativex/TypeTests$Finder",iterator.next());
		assertEquals("org/springframework/nativex/TypeTests$TXClass4",iterator.next());
		Type extender2 = typeSystem.resolveName(Extender2.class.getName());
		typesInSignature = extender2.getTypesInSignature();
		iterator = typesInSignature.iterator();
		assertEquals(2, typesInSignature.size());
		assertEquals("java/io/Serializable",iterator.next());
		assertEquals("org/springframework/nativex/TypeTests$TXClass4",iterator.next());
	}
	
	@Test
	public void testFields() {
		Type t = typeSystem.resolveName(TestFields.class.getName());
		Field one = t.getField("one");
		Set<String> tis = one.getTypesInSignature();
		Iterator<String> iterator = tis.iterator();
		assertEquals(1,tis.size());
		assertEquals("java/lang/String",iterator.next());

		Field two = t.getField("two");
		tis = two.getTypesInSignature();
		iterator = tis.iterator();
		assertEquals(2,tis.size());
		assertEquals("java/lang/Integer",iterator.next());
		assertEquals("java/util/List",iterator.next());

		Field three = t.getField("three");
		tis = three.getTypesInSignature();
		iterator = tis.iterator();
		assertEquals(0,tis.size());

		Field four = t.getField("four");
		tis = four.getTypesInSignature();
		iterator = tis.iterator();
		assertEquals(1,tis.size());
		assertEquals("java/lang/String",iterator.next());
	}

	@Test
	public void testTypesInSignatureForMethods() {
		Type t = typeSystem.resolveName(TestMethods.class.getName());
		Method foo = t.getMethod("foo").get(0);
		Set<String> tis = foo.getTypesInSignature();
		Iterator<String> iterator = tis.iterator();
		assertEquals("java/lang/String", iterator.next());
		assertEquals("java/util/List", iterator.next());
		assertFalse(iterator.hasNext());

		Method bar = t.getMethod("bar").get(0);
		tis = bar.getTypesInSignature();
		iterator = tis.iterator();
		assertEquals("java/lang/String", iterator.next());
		assertEquals("java/util/List", iterator.next());
		assertFalse(iterator.hasNext());

		Method boo = t.getMethod("boo").get(0);
		tis = boo.getTypesInSignature();
		iterator = tis.iterator();
		assertEquals("java/lang/String", iterator.next());
		assertFalse(iterator.hasNext());
	}
	
	static class TestFields {
		String one;
		List<Integer> two;
		int[] three;
		String[] four;
	}

	@Test
	public void parameterCount() {
		Type t = typeSystem.resolveName(TestMethods.class.getName());

		// public void one(String a) {}
		Method one = t.getMethod("one").get(0);
		assertEquals(1,one.getParameterCount());
		assertEquals("java.lang.String", one.getParameterTypes().get(0).getDottedName());

		// public void two(java.io.Serializable a,String b) {}
		Method two = t.getMethod("two").get(0);
		assertEquals(2,two.getParameterCount());
		assertEquals("java.io.Serializable", two.getParameterTypes().get(0).getDottedName());
		assertEquals("java.lang.String", two.getParameterTypes().get(1).getDottedName());
	}
	
	@Test
	public void testMethodsToArray() {
		Type t = typeSystem.resolveName(TestMethods.class.getName());
		Method one = t.getMethod("one").get(0);
		String[] array = one.asConfigurationArray();
		assertEquals(2,array.length);
		assertEquals("one",array[0]);
		assertEquals("java.lang.String",array[1]);
		Method two = t.getMethod("two").get(0);
		array = two.asConfigurationArray();
		assertEquals(3,array.length);
		assertEquals("two",array[0]);
		assertEquals("java.io.Serializable",array[1]);
		assertEquals("java.lang.String",array[2]);
	}
	
	@SuppressWarnings("rawtypes")
	static class TestMethods {
		public void one(String a) {}
		public void two(java.io.Serializable a,String b) {}
		public int foo(String s1, String[] s2, List<String> s3) { return 0; }
		public int bar(String s1, List[] s2, float f) { return 0; }
		public String boo() { return ""; }
	}
	
	@Test
	public void testMethodsToDescriptors() {
		Type t = typeSystem.resolveName(TestMethodsDescriptors.class.getName());
		MethodDescriptor descriptor = t.getMethod("aaa").get(0).getMethodDescriptor();
		assertEquals("aaa",descriptor.getName());
		List<String> parameterTypes = descriptor.getParameterTypes();
		assertEquals(0,parameterTypes.size());
		descriptor = t.getMethod("bbb").get(0).getMethodDescriptor();
		assertEquals("bbb",descriptor.getName());
		parameterTypes = descriptor.getParameterTypes();
		assertEquals(4,parameterTypes.size());
		assertEquals("java.lang.String",parameterTypes.get(0));
		assertEquals("int",parameterTypes.get(1));
		assertEquals("long[]",parameterTypes.get(2));
		assertEquals("java.lang.String[]",parameterTypes.get(3));
	}
	
	static class TestMethodsDescriptors {
		public void aaa() {}
		public void bbb(String s, int i, long[] ls, String[] ss) {}
		
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

//	@Transactional
	static class TXClass1 {
	}

	static class TXClass2 {
//		@Transactional
		public void m() {}
	}

	@NativeHint(trigger = Integer.class)
	static class TestClass1 {
	}

	@NativeHint
	static class TestClass1a {
	}

	@NativeHint(types = { @TypeHint(types = { String[].class }) })
	static class TestClass1b {
	}

	@NativeHint(trigger = Integer.class, types = { @TypeHint(types = { String.class, Float.class }) })
	static class TestClass2 {
	}

	@NativeHint(trigger = Integer.class, types = { @TypeHint(types = { String.class }) })
	@NativeHint(trigger = String.class, types = { @TypeHint(types = { Float.class }) })
	static class TestClass3 {
	}

	@NativeHint(trigger = Integer.class, abortIfTypesMissing = true)
	static class TestClass4 {
	}

	@NativeHint(trigger = String.class, types = {
			@TypeHint(types = { Float.class }, access = AccessBits.CLASS) })
	static class TestClass5 {
	}

	@NativeHint(trigger = String.class, types = {
			@TypeHint(types = { Float.class }, access = AccessBits.CLASS),
			@TypeHint(types = { Integer.class }, access = AccessBits.RESOURCE) })
	static class TestClass6 {
	}

	@NativeHint(trigger = String.class, types = {
			@TypeHint(typeNames = { "java.lang.String" }, types = { Float.class }, access = AccessBits.CLASS) })
	static class TestClass7 {
	}

	@NativeHint(types = { @TypeHint(types = {String[].class},typeNames = { "org.springframework.nativex.TypeTests$TestClass6",
			"org.springframework.nativex.TypeTests$TestClass7[]", }) })
	static class TestClass8 {

	}
}
