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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.graalvm.domain.init.InitializationDescriptor;
import org.springframework.graalvm.domain.reflect.FieldDescriptor;
import org.springframework.graalvm.extension.FieldInfo;
import org.springframework.graalvm.extension.InitializationInfo;
import org.springframework.graalvm.extension.InitializationTime;
import org.springframework.graalvm.extension.MethodInfo;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.ProxyInfo;
import org.springframework.graalvm.extension.ResourcesInfo;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.support.Mode;
import org.springframework.graalvm.type.AccessBits;
import org.springframework.graalvm.type.AccessDescriptor;
import org.springframework.graalvm.type.Hint;
import org.springframework.graalvm.type.ProxyDescriptor;
import org.springframework.graalvm.type.ResourcesDescriptor;
import org.springframework.graalvm.type.Type;
import org.springframework.graalvm.type.TypeSystem;

public class HintTests {

	static TypeSystem typeSystem;

	@BeforeClass
	public static void setup() throws Exception {
		File file = new File("./target/classes");
		// System.out.println(file.getCanonicalPath());
		typeSystem = new TypeSystem(Collections.singletonList(file.toString()));
	}

	@Test
	public void hints() {
		Type testClass = typeSystem.resolveName(TestClass1.class.getName());
		List<Hint> hints = testClass.getHints();
		assertEquals(1,hints.size());
		Map<String, AccessDescriptor> specificTypes = hints.get(0).getSpecificTypes();
		System.out.println(specificTypes);
		AccessDescriptor accessDescriptor = specificTypes.get("java.lang.String[]");
		assertNotNull(accessDescriptor);
	}

	@NativeImageHint(typeInfos = { @TypeInfo(types = { String[].class }) })
	static class TestClass1 {
	}
	
	@Test
	public void proxies() {
		Type testClass = typeSystem.resolveName(TestClass2.class.getName());
		List<Hint> hints = testClass.getHints();
		assertEquals(1,hints.size());
		Hint hint = hints.get(0);
		List<ProxyDescriptor> proxies= hint.getProxyDescriptors();
		assertEquals(1,proxies.size());
		String[] types = proxies.get(0).getTypes();
		assertEquals("java.lang.String",types[0]);
		assertEquals("java.lang.Integer",types[1]);
	}

	@NativeImageHint(proxyInfos = { @ProxyInfo(types = { String.class,Integer.class }) })
	static class TestClass2 {
	}

	@Test
	public void resources() {
		Type testClass = typeSystem.resolveName(TestClass3.class.getName());
		List<Hint> hints = testClass.getHints();
		assertEquals(1,hints.size());
		Hint hint = hints.get(0);
		List<ResourcesDescriptor> resourcesDescriptors = hint.getResourceDescriptors();
		assertEquals(2,resourcesDescriptors.size());
		String[] patterns = resourcesDescriptors.get(0).getPatterns();
		assertEquals("aaa",patterns[0]);
		assertEquals("bbb",patterns[1]);
		assertFalse(resourcesDescriptors.get(0).isBundle());
		patterns = resourcesDescriptors.get(1).getPatterns();
		assertEquals("ccc",patterns[0]);
		assertTrue(resourcesDescriptors.get(1).isBundle());
	}

	@NativeImageHint(resourcesInfos = { 
			@ResourcesInfo(patterns = { "aaa","bbb" }),
			@ResourcesInfo(patterns = { "ccc" }, isBundle = true)
	})
	static class TestClass3 {
	}

	@Test
	public void modeRestrictions() {
		Type testClass = typeSystem.resolveName(TestClass4.class.getName());
		List<Hint> hints = testClass.getHints();
		assertEquals(1,hints.size());
		Hint hint = hints.get(0);
		List<Mode> modes = hint.getModes();
		assertEquals(3,modes.size());
		assertEquals("AGENT",modes.get(0).toString());
		assertEquals("FUNCTIONAL",modes.get(1).toString());
		assertEquals("DEFAULT",modes.get(2).toString());
	}

	@NativeImageHint(modes= {Mode.AGENT, Mode.FUNCTIONAL, Mode.DEFAULT}, resourcesInfos = { 
			@ResourcesInfo(patterns = { "aaa","bbb" })
	})
	static class TestClass4 {
	}

	@Test
	public void initializations() {
		Type testClass = typeSystem.resolveName(TestClass5.class.getName());
		List<Hint> hints = testClass.getHints();
		assertEquals(1,hints.size());
		Hint hint = hints.get(0);
		List<InitializationDescriptor> initializationDescriptors = hint.getInitializationDescriptors();
		assertEquals(2,initializationDescriptors.size());
		List<String> btc = initializationDescriptors.get(0).getBuildtimeClasses();
		List<String> btp = initializationDescriptors.get(0).getBuildtimePackages();
		List<String> rtc = initializationDescriptors.get(0).getRuntimeClasses();
		List<String> rtp = initializationDescriptors.get(0).getRuntimePackages();
		assertEquals(0,btc.size());
		assertEquals(3,rtc.size());
		assertEquals("aaa",rtc.get(0));
		assertEquals("bbb",rtc.get(1));
		assertEquals("java.lang.String",rtc.get(2));
		assertEquals(0,btp.size());
		assertEquals(0,rtp.size());
		btc = initializationDescriptors.get(1).getBuildtimeClasses();
		btp = initializationDescriptors.get(1).getBuildtimePackages();
		rtc = initializationDescriptors.get(1).getRuntimeClasses();
		rtp = initializationDescriptors.get(1).getRuntimePackages();
		assertEquals(0,btc.size());
		assertEquals(0,rtc.size());
		assertEquals(1,btp.size());
		assertEquals("ccc",btp.get(0));
		assertEquals(0,rtp.size());
	}

	@NativeImageHint(initializationInfos = { 
			@InitializationInfo(
					typeNames = { "aaa","bbb" }, 
					types=String.class,
					initTime = InitializationTime.RUN),
			@InitializationInfo(
					packageNames = { "ccc" },
					initTime = InitializationTime.BUILD)
	})
	static class TestClass5 {
	}

	@Test
	public void methods() {
		Type testClass = typeSystem.resolveName(TestClass6.class.getName());
		List<Hint> hints = testClass.getHints();
		assertEquals(1,hints.size());
		Hint hint = hints.get(0);
		Map<String, AccessDescriptor> specificTypes = hint.getSpecificTypes();
		AccessDescriptor accessDescriptor = specificTypes.get("java.lang.String");
		assertNotNull(accessDescriptor);
		assertEquals((Integer)AccessBits.FULL_REFLECTION,accessDescriptor.getAccessBits());
		assertEquals("foo(java.lang.String)",accessDescriptor.getMethodDescriptors().get(0).toString());
		assertEquals(0,accessDescriptor.getFieldDescriptors().size());
	}

	@NativeImageHint(typeInfos = {
		@TypeInfo(typeNames = "java.lang.String",
				methods= @MethodInfo(name="foo",parameterTypes = String.class)
		)
	})
	static class TestClass6 {
	}

	@Test
	public void fields() {
		Type testClass = typeSystem.resolveName(TestClass7.class.getName());
		List<Hint> hints = testClass.getHints();
		assertEquals(1,hints.size());
		Hint hint = hints.get(0);
		Map<String, AccessDescriptor> specificTypes = hint.getSpecificTypes();
		AccessDescriptor accessDescriptor = specificTypes.get("java.lang.String");
		assertNotNull(accessDescriptor);
		assertEquals((Integer)AccessBits.FULL_REFLECTION,accessDescriptor.getAccessBits());
		assertEquals(0,accessDescriptor.getMethodDescriptors().size());
		org.springframework.graalvm.type.FieldDescriptor fd = accessDescriptor.getFieldDescriptors().get(0);
		assertEquals("foo",fd.getName());
		assertTrue(fd.isAllowUnsafeAccess());
	}

	@NativeImageHint(typeInfos = {
		@TypeInfo(typeNames = "java.lang.String",
				fields= @FieldInfo(name="foo",allowUnsafeAccess = true)
		)
	})
	static class TestClass7 {
	}

}
