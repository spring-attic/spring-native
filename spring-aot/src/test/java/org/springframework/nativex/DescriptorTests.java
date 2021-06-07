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

import org.junit.jupiter.api.Test;
import org.springframework.nativex.domain.init.InitializationDescriptor;
import org.springframework.nativex.domain.proxies.ProxiesDescriptor;
import org.springframework.nativex.domain.proxies.JdkProxyDescriptor;
import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.domain.reflect.ReflectionDescriptor;
import org.springframework.nativex.domain.resources.ResourcesDescriptor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

public class DescriptorTests {

	@Test
	public void initializationDescriptors() {
		InitializationDescriptor d = new InitializationDescriptor();

		assertThat(d.isEmpty()).isTrue();
		d.addBuildtimePackage("com.foo");
		assertThat(d.isEmpty()).isFalse();
		d.addRuntimePackage("com.bar");
		assertThat(d.isEmpty()).isFalse();
		d.addBuildtimeClass("java.lang.String");
		assertThat(d.isEmpty()).isFalse();
		d.addBuildtimeClass("java.util.List");
		assertThat(d.isEmpty()).isFalse();
		d.addRuntimeClass("java.lang.Integer");
		assertThat(d.isEmpty()).isFalse();
		d.addRuntimeClass("java.lang.Boolean");
		assertThat(d.isEmpty()).isFalse();

		assertThat(d.getRuntimeClasses()).hasSize(2).contains("java.lang.Integer").contains("java.lang.Boolean");
		assertThat(d.getBuildtimeClasses()).hasSize(2).contains("java.lang.String").contains("java.util.List");
		assertThat(d.getBuildtimePackages()).hasSize(1).contains("com.foo");
		assertThat(d.getRuntimePackages()).hasSize(1).contains("com.bar");
		assertThat(d.toDetailedString())
			.contains("#2 buildtime-init-classes   #1 buildtime-init-packages   #2 runtime-init-classes   #1 runtime-init-packages")
			.contains("com.foo")
			.contains("java.util.List");

		InitializationDescriptor d2 = new InitializationDescriptor(d);
		assertThat(d2.getRuntimeClasses()).hasSize(2).contains("java.lang.Integer").contains("java.lang.Boolean");
		assertThat(d2.getBuildtimeClasses()).hasSize(2).contains("java.lang.String").contains("java.util.List");
		assertThat(d2.getBuildtimePackages()).hasSize(1).contains("com.foo");
		assertThat(d2.getRuntimePackages()).hasSize(1).contains("com.bar");
		
		assertThat(d.toString()).contains("#2 buildtime-init-classes   #1 buildtime-init-packages   #2 runtime-init-classes   #1 runtime-init-packages");
	}
	
	@Test
	public void initializationDescriptorReadingWriting() {
		InitializationDescriptor d = new InitializationDescriptor();
		d.addBuildtimePackage("com.foo");
		d.addRuntimePackage("com.bar");
		d.addBuildtimeClass("java.lang.String");
		d.addBuildtimeClass("java.util.List");
		d.addRuntimeClass("java.lang.Integer");
		d.addRuntimeClass("java.lang.Boolean");
		String json = d.toJSON();
		InitializationDescriptor d2 = InitializationDescriptor.fromJSON(json);
		assertThat(d.toDetailedString()).isEqualTo(d2.toDetailedString());
	}

	@Test
	public void proxiesDescriptor() {
		JdkProxyDescriptor d = new JdkProxyDescriptor(Collections.singletonList("java.io.Serializable"));
		assertThat(d.getTypes()).hasSize(1).contains("java.io.Serializable");
		ProxiesDescriptor pd = new ProxiesDescriptor();
		pd.add(d);
		String json = pd.toJSON();
		ProxiesDescriptor pd2 = ProxiesDescriptor.fromJSON(json);
		assertThat(pd.toString()).isEqualTo(pd2.toString());
		assertThat(d.containsInterface("java.io.Serializable")).isTrue();
		assertThat(d.compareTo(pd2.getProxyDescriptors().get(0))).isEqualTo(0);
		assertThat(d.equals(pd2.getProxyDescriptors().get(0))).isTrue();
	}

	@Test
	public void resourcesDescriptor() {
		ResourcesDescriptor d = new ResourcesDescriptor();
		d.add("aaa/bbb/*.class");
		assertThat(d.getPatterns()).hasSize(1).contains("aaa/bbb/*.class");
		assertThat(d.getBundles()).isEmpty();
		String json = d.toJSON();
		ResourcesDescriptor d2 = ResourcesDescriptor.fromJSON(json);
		assertThat(d.toString()).isEqualTo(d2.toString());
	}
	
	@Test
	public void reflectionDescriptorMerge() {
		ClassDescriptor one = ClassDescriptor.of("one");
		ClassDescriptor two = ClassDescriptor.of("two");
		ReflectionDescriptor a = new ReflectionDescriptor();
		a.add(one);
		ReflectionDescriptor b = new ReflectionDescriptor();
		b.add(two);
		a.merge(b);
		assertThat(a.getClassDescriptors()).containsExactly(one, two);
	}

	@Test
	public void resourcesDescriptorMerge() {
		ResourcesDescriptor a = new ResourcesDescriptor();
		a.add("foo/one.json");
		ResourcesDescriptor b = new ResourcesDescriptor();
		b.add("bar/two.json");
		a.merge(b);
		assertThat(a.getPatterns()).containsExactlyInAnyOrder("foo/one.json", "bar/two.json");
	}

}
