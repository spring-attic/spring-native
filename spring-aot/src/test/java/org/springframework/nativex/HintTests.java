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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.nativex.domain.init.InitializationDescriptor;
import org.springframework.nativex.domain.proxies.AotProxyDescriptor;
import org.springframework.nativex.domain.proxies.JdkProxyDescriptor;
import org.springframework.nativex.domain.reflect.FieldDescriptor;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.AotProxyHint;
import org.springframework.nativex.hint.FieldHint;
import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.hint.MethodHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ProxyBits;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.AccessDescriptor;
import org.springframework.nativex.type.HintApplication;
import org.springframework.nativex.type.HintDeclaration;
import org.springframework.nativex.type.ResourcesDescriptor;
import org.springframework.nativex.type.Type;
import org.springframework.nativex.type.TypeSystem;

public class HintTests {

	static TypeSystem typeSystem;

	@BeforeAll
	public static void setup() throws Exception {
		File file = new File("./target/classes");
		typeSystem = new TypeSystem(Collections.singletonList(file.toString()));
	}

	@Test
	public void hints() {
		Type testClass = typeSystem.resolveName(TestClass1.class.getName());
		List<HintApplication> hints = testClass.getApplicableHints();
		assertEquals(1,hints.size());
		Map<String, AccessDescriptor> specificTypes = hints.get(0).getSpecificTypes();
		AccessDescriptor accessDescriptor = specificTypes.get("java.lang.String[]");
		assertThat(accessDescriptor).isNotNull();
		assertThat(accessDescriptor.getAccessBits()).isEqualTo(AccessBits.CLASS);
	}

	@Test
	public void directHints() {
		Type t = typeSystem.resolveName(TH1.class.getName());
		List<HintDeclaration> hints = t.getCompilationHints();
		assertEquals(1,hints.size());
		HintDeclaration hd = hints.get(0);
		assertEquals(TH1.class.getName(),hd.getTriggerTypename());
		assertTrue(hd.getDependantTypes().containsKey("java.lang.String"));

		t = typeSystem.resolveName(TH2.class.getName());
		hd = t.getCompilationHints().get(0);
		assertEquals(TH2.class.getName(),hd.getTriggerTypename());
		assertTrue(hd.getDependantTypes().containsKey("java.lang.String"));
		assertTrue(hd.getDependantTypes().containsKey("java.lang.Integer"));
	}

	@NativeHint(types = { @TypeHint(types = { String[].class }) })
	static class TestClass1 {
	}

	@TypeHint(types=String.class)
	static class TH1 {
	}

	@TypeHint(types=String.class)
	@TypeHint(types=Integer.class)
	static class TH2 {
	}

	@Test
	public void proxies() {
		Type testClass = typeSystem.resolveName(TestClass2.class.getName());
		List<HintApplication> hints = testClass.getApplicableHints();
		assertEquals(1,hints.size());
		HintApplication hint = hints.get(0);
		List<JdkProxyDescriptor> proxies= hint.getProxyDescriptors();
		assertEquals(1,proxies.size());
		String[] types = proxies.get(0).getTypes().toArray(new String[] {}); 
		assertEquals("java.lang.String",types[0]);
		assertEquals("java.lang.Integer",types[1]);
	}

	@NativeHint(jdkProxies = { @JdkProxyHint(types = { String.class,Integer.class }) })
	static class TestClass2 {
	}

	@Test
	public void resources() {
		Type testClass = typeSystem.resolveName(TestClass3.class.getName());
		List<HintApplication> hints = testClass.getApplicableHints();
		assertEquals(1,hints.size());
		HintApplication hint = hints.get(0);
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

	@NativeHint(resources = {
			@ResourceHint(patterns = { "aaa","bbb" }),
			@ResourceHint(patterns = { "ccc" }, isBundle = true)
	})
	static class TestClass3 {
	}

	@Test
	public void initializations() {
		Type testClass = typeSystem.resolveName(TestClass5.class.getName());
		List<HintApplication> hints = testClass.getApplicableHints();
		assertEquals(1,hints.size());
		HintApplication hint = hints.get(0);
		List<InitializationDescriptor> initializationDescriptors = hint.getInitializationDescriptors();
		assertEquals(2,initializationDescriptors.size());
		Set<String> btc = initializationDescriptors.get(0).getBuildtimeClasses();
		Set<String> btp = initializationDescriptors.get(0).getBuildtimePackages();
		Set<String> rtc = initializationDescriptors.get(0).getRuntimeClasses();
		Set<String> rtp = initializationDescriptors.get(0).getRuntimePackages();
		assertEquals(0,btc.size());
		assertEquals(3,rtc.size());
		assertContains("aaa",rtc);
		assertContains("bbb",rtc);
		assertContains("java.lang.String",rtc);
		assertEquals(0,btp.size());
		assertEquals(0,rtp.size());
		btc = initializationDescriptors.get(1).getBuildtimeClasses();
		btp = initializationDescriptors.get(1).getBuildtimePackages();
		rtc = initializationDescriptors.get(1).getRuntimeClasses();
		rtp = initializationDescriptors.get(1).getRuntimePackages();
		assertEquals(0,btc.size());
		assertEquals(0,rtc.size());
		assertEquals(1,btp.size());
		assertContains("ccc",btp);
		assertEquals(0,rtp.size());
	}

	private void assertContains(String string, Set<String> set) {
		boolean found = false;
		if (set != null) {
			for (String s: set) {
				if (s.equals(string)) {
					found = true;
				}
			}
		}
		if (!found) {
			fail("Did not find '"+string+"' in "+set);
		}
	}

	@NativeHint(initialization = {
			@InitializationHint(
					typeNames = { "aaa","bbb" },
					types=String.class,
					initTime = InitializationTime.RUN),
			@InitializationHint(
					packageNames = { "ccc" },
					initTime = InitializationTime.BUILD)
	})
	static class TestClass5 {
	}

	@Test
	public void methods() {
		Type testClass = typeSystem.resolveName(TestClass6.class.getName());
		List<HintApplication> hints = testClass.getApplicableHints();
		assertEquals(1,hints.size());
		HintApplication hint = hints.get(0);
		Map<String, AccessDescriptor> specificTypes = hint.getSpecificTypes();
		AccessDescriptor accessDescriptor = specificTypes.get("java.lang.String");
		assertNotNull(accessDescriptor);
		assertEquals((Integer) AccessBits.FULL_REFLECTION,accessDescriptor.getAccessBits());
		assertEquals("foo(java.lang.String)",accessDescriptor.getMethodDescriptors().get(0).toString());
		assertEquals(0,accessDescriptor.getFieldDescriptors().size());
	}

	@NativeHint(types = {
		@TypeHint(typeNames = "java.lang.String",
				methods= @MethodHint(name="foo",parameterTypes = String.class)
		)
	})
	static class TestClass6 {
	}

	@Test
	public void fields() {
		Type testClass = typeSystem.resolveName(TestClass7.class.getName());
		List<HintApplication> hints = testClass.getApplicableHints();
		assertEquals(1,hints.size());
		HintApplication hint = hints.get(0);
		Map<String, AccessDescriptor> specificTypes = hint.getSpecificTypes();
		AccessDescriptor accessDescriptor = specificTypes.get("java.lang.String");
		assertNotNull(accessDescriptor);
		assertEquals((Integer)AccessBits.FULL_REFLECTION,accessDescriptor.getAccessBits());
		assertEquals(0,accessDescriptor.getMethodDescriptors().size());
		FieldDescriptor fd = accessDescriptor.getFieldDescriptors().get(0);
		assertEquals("foo",fd.getName());
		assertTrue(fd.isAllowUnsafeAccess());
	}

	@NativeHint(types = {
		@TypeHint(typeNames = "java.lang.String",
				fields= @FieldHint(name="foo",allowUnsafeAccess = true)
		)
	})
	static class TestClass7 {
	}

	@Test
	public void classProxyDescriptors() {
		Type t = typeSystem.resolveName(TestClass8.class.getName());
		List<HintApplication> hints = t.getApplicableHints();
		assertEquals(1,hints.size());
		HintApplication hint = hints.get(0);
		List<JdkProxyDescriptor> proxyDescriptors = hint.getProxyDescriptors();
		assertThat(proxyDescriptors).hasSize(1);
		assertThat(proxyDescriptors.get(0).isClassProxy()).isTrue();
		AotProxyDescriptor cpd = (AotProxyDescriptor)proxyDescriptors.get(0);
		assertThat(cpd.getTargetClassType()).isEqualTo("java.lang.String");
		assertThat(cpd.getInterfaceTypes().get(0)).isEqualTo("java.io.Serializable");
		assertThat(cpd.getProxyFeatures()).isEqualTo(ProxyBits.EXPOSE_PROXY);
	}

	@NativeHint(
		aotProxies = @AotProxyHint(targetClass = String.class, interfaces = Serializable.class, proxyFeatures = ProxyBits.EXPOSE_PROXY)
	)
	static class TestClass8 {
	}

	@Test
	public void classProxyDescriptors2() {
		Type t = typeSystem.resolveName(TestClass9.class.getName());
		List<HintApplication> hints = t.getApplicableHints();
		assertEquals(1,hints.size());
		HintApplication hint = hints.get(0);
		List<JdkProxyDescriptor> proxyDescriptors = hint.getProxyDescriptors();
		assertThat(proxyDescriptors).hasSize(1);
		assertThat(proxyDescriptors.get(0).isClassProxy()).isTrue();
		AotProxyDescriptor cpd = (AotProxyDescriptor)proxyDescriptors.get(0);
		assertThat(cpd.getTargetClassType()).isEqualTo("java.lang.String");
		assertThat(cpd.getInterfaceTypes().get(0)).isEqualTo("java.util.List");
		assertThat(cpd.getProxyFeatures()).isEqualTo(ProxyBits.EXPOSE_PROXY);
	}

	@NativeHint(
		aotProxies = @AotProxyHint(targetClassName = "java.lang.String", interfaceNames = "java.util.List", proxyFeatures = ProxyBits.EXPOSE_PROXY)
	)
	static class TestClass9 {
	}
}
