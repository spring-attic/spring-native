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

package org.springframework.aot.context.bootstrap.generator.infrastructure.nativex;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import org.junit.jupiter.api.Test;

import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link NativeConfigurationRegistry}.
 *
 * @author Stephane Nicoll
 * @author Sebastien Deleuze
 */
class NativeConfigurationRegistryTests {

	private final NativeConfigurationRegistry registry = new NativeConfigurationRegistry();

	@Test
	void forTypeRegisterClass() {
		registry.reflection().forType(TestClass.class);
		assertTestClassEmptyEntry();
	}

	@Test
	void forTypeWithProxyRegisterUserClass() {
		ProxyFactory pf = new ProxyFactory();
		pf.setTargetClass(TestClass.class);
		Object proxy = pf.getProxy();
		registry.reflection().forType(proxy.getClass());
		assertTestClassEmptyEntry();
	}

	private void assertTestClassEmptyEntry() {
		assertThat(registry.reflection().reflectionEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(TestClass.class);
			assertThat(entry.getMethods()).isEmpty();
			assertThat(entry.getFields()).isEmpty();
		});
	}

	@Test
	void addMethodUseDeclaringClass() {
		Method method = ReflectionUtils.findMethod(TestClass.class, "setName", String.class);
		assertTestClassMethod(method);
	}

	@Test
	void addMethodWithProxyUseUserClass() {
		ProxyFactory pf = new ProxyFactory();
		pf.setTargetClass(TestClass.class);
		Object proxy = pf.getProxy();
		Method method = ReflectionUtils.findMethod(proxy.getClass(), "setName", String.class);
		assertTestClassMethod(method);
	}

	private void assertTestClassMethod(Method method) {
		registry.reflection().addExecutable(method);
		assertThat(registry.reflection().reflectionEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(TestClass.class);
			assertThat(entry.getMethods()).containsOnly(method);
			assertThat(entry.getFields()).isEmpty();
		});
	}

	@Test
	void addFieldUseDeclaringClass() {
		Field field = ReflectionUtils.findField(TestClass.class, "field");
		registry.reflection().addField(field);
		assertThat(registry.reflection().reflectionEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(TestClass.class);
			assertThat(entry.getMethods()).isEmpty();
			assertThat(entry.getFields()).contains(field);
		});
	}

	@Test
	void getClassDescriptorsMapEntries() {
		registry.reflection().forType(String.class).withFields(ReflectionUtils.findField(String.class, "value"));
		registry.reflection().forType(Integer.class).withExecutables(ReflectionUtils.findMethod(Integer.class, "decode", String.class));
		List<ClassDescriptor> classDescriptors = registry.reflection().toClassDescriptors();
		assertThat(classDescriptors).hasSize(2);
		assertThat(classDescriptors).anySatisfy((descriptor) -> {
			assertThat(descriptor.getName()).isEqualTo(String.class.getName());
			assertThat(descriptor.getMethods()).isNull();
			assertThat(descriptor.getFields()).singleElement().satisfies((fieldDescriptor) ->
					assertThat(fieldDescriptor.getName()).isEqualTo("value"));
		});
		assertThat(classDescriptors).anySatisfy((descriptor) -> {
			assertThat(descriptor.getName()).isEqualTo(Integer.class.getName());
			assertThat(descriptor.getMethods()).singleElement().satisfies((methodDescriptor) ->
					assertThat(methodDescriptor.getName()).isEqualTo("decode"));
			assertThat(descriptor.getFields()).isNull();
		});
	}

	@Test
	void getClassDescriptorsMapAllEntries() {
		registry.reflection().forType(String.class).withFields(ReflectionUtils.findField(String.class, "value"));
		registry.reflection().forGeneratedType(ClassName.get("com.example", "Test")).withFields(FieldSpec.builder(String.class, "generated").build());
		registry.reflection().forType(Integer.class).withExecutables(ReflectionUtils.findMethod(Integer.class, "decode", String.class));
		List<ClassDescriptor> classDescriptors = registry.reflection().toClassDescriptors();
		assertThat(classDescriptors).anySatisfy((descriptor) -> {
			assertThat(descriptor.getName()).isEqualTo(String.class.getName());
			assertThat(descriptor.getMethods()).isNull();
			assertThat(descriptor.getFields()).singleElement().satisfies((fieldDescriptor) ->
					assertThat(fieldDescriptor.getName()).isEqualTo("value"));
		});
		assertThat(classDescriptors).anySatisfy((descriptor) -> {
			assertThat(descriptor.getName()).isEqualTo(Integer.class.getName());
			assertThat(descriptor.getMethods()).singleElement().satisfies((methodDescriptor) ->
					assertThat(methodDescriptor.getName()).isEqualTo("decode"));
			assertThat(descriptor.getFields()).isNull();
		});
		assertThat(classDescriptors).anySatisfy((descriptor) -> {
			assertThat(descriptor.getName()).isEqualTo("com.example.Test");
			assertThat(descriptor.getMethods()).isNull();
			assertThat(descriptor.getFields()).singleElement().satisfies((fieldDescriptor) ->
					assertThat(fieldDescriptor.getName()).isEqualTo("generated"));
		});
		assertThat(classDescriptors).hasSize(3);
	}

	@Test
	void addResource() {
		registry.resources().add(NativeResourcesEntry.ofClassName("java.lang.String"));
		assertThat(registry.resources().toResourcesDescriptor().getPatterns()).singleElement().satisfies((pattern) ->
				assertThat(pattern).isEqualTo("java/lang/String.class"));
	}

	@Test
	void addSeveralResources() {
		registry.resources().add(NativeResourcesEntry.ofClassName("java.lang.String"));
		registry.resources().add(NativeResourcesEntry.ofClassName("java.lang.Integer"));
		registry.resources().add(NativeResourcesEntry.ofClassName("java.lang.String"));
		Set<String> patterns = registry.resources().toResourcesDescriptor().getPatterns();
		assertThat(patterns).anySatisfy((pattern) ->
				assertThat(pattern).isEqualTo("java/lang/String.class"));
		assertThat(patterns).anySatisfy((pattern) ->
				assertThat(pattern).isEqualTo("java/lang/Integer.class"));
		assertThat(patterns).hasSize(2);
	}

	@Test
	void addProxy() {
		registry.proxy().add(NativeProxyEntry.ofInterfaces(SpringProxy.class, Advised.class));
		assertThat(registry.proxy().toProxiesDescriptor()).satisfies((proxiesDescriptor) ->
				assertThat(proxiesDescriptor.getProxyDescriptors()).singleElement().satisfies(jdkProxyDescriptor ->
						assertThat(jdkProxyDescriptor.getTypes()).containsExactly(SpringProxy.class.getName(), Advised.class.getName())));
	}

	@Test
	void addSeveralProxies() {
		registry.proxy().add(NativeProxyEntry.ofInterfaces(SpringProxy.class));
		registry.proxy().add(NativeProxyEntry.ofInterfaces(SpringProxy.class, Advised.class));
		assertThat(registry.proxy().toProxiesDescriptor()).satisfies((proxiesDescriptor) ->
				assertThat(proxiesDescriptor.getProxyDescriptors()).anySatisfy(jdkProxyDescriptor ->
								assertThat(jdkProxyDescriptor.getTypes()).containsExactly(SpringProxy.class.getName(), Advised.class.getName()))
						.anySatisfy(jdkProxyDescriptor -> assertThat(jdkProxyDescriptor.getTypes()).containsExactly(SpringProxy.class.getName())).hasSize(2)
		);
	}

	@Test
	void addInitialization() {
		registry.initialization().add(NativeInitializationEntry.ofRuntimeType(String.class));
		assertThat(registry.initialization().toInitializationDescriptor()).satisfies((initializationDescriptor) ->
				assertThat(initializationDescriptor.getRuntimeClasses()).singleElement().isEqualTo(String.class.getName()));
	}

	@Test
	void addSeveralInitializations() {
		registry.initialization().add(NativeInitializationEntry.ofRuntimeType(String.class));
		registry.initialization().add(NativeInitializationEntry.ofBuildTimePackage("foo.bar"));
		assertThat(registry.initialization().toInitializationDescriptor()).satisfies((initializationDescriptor) -> {
			assertThat(initializationDescriptor.getRuntimeClasses()).singleElement().isEqualTo(String.class.getName());
			assertThat(initializationDescriptor.getBuildtimePackages()).singleElement().isEqualTo("foo.bar");
		});
	}

	@Test
	void addSerialization() {
		registry.serialization().add(NativeSerializationEntry.ofType(String.class));
		assertThat(registry.serialization().toSerializationDescriptor()).satisfies((serializationDescriptor) ->
				assertThat(serializationDescriptor.getSerializableTypes()).singleElement().isEqualTo(String.class.getName()));
	}

	@Test
	void addLambdaCapturingSerialization() {
		registry.serialization().add(NativeSerializationEntry.ofLambdaCapturingType(String.class));
		assertThat(registry.serialization().toSerializationDescriptor()).satisfies((serializationDescriptor) ->
				assertThat(serializationDescriptor.getSerializableLambdaCapturingTypes()).singleElement().isEqualTo(String.class.getName()));
	}

	@Test
	void addSeveralSerializations() {
		registry.serialization().add(NativeSerializationEntry.ofType(String.class));
		registry.serialization().add(NativeSerializationEntry.ofType(Long.class));
		registry.serialization().add(NativeSerializationEntry.ofLambdaCapturingType(String.class));
		registry.serialization().add(NativeSerializationEntry.ofLambdaCapturingType(Long.class));
		assertThat(registry.serialization().toSerializationDescriptor()).satisfies((serializationDescriptor) ->
				assertThat(serializationDescriptor.getSerializableTypes()).containsOnly(String.class.getName(), Long.class.getName()));
		assertThat(registry.serialization().toSerializationDescriptor()).satisfies((serializationDescriptor) ->
				assertThat(serializationDescriptor.getSerializableLambdaCapturingTypes()).containsOnly(String.class.getName(), Long.class.getName()));
	}

	@Test
	void addOption() {
		registry.options().add("-H:+PrintAnalysisCallTree");
		assertThat(registry.options()).containsExactly("-H:+PrintAnalysisCallTree");
	}

	@Test
	void addSeveralOptions() {
		registry.options().add("-H:+PrintAnalysisCallTree");
		registry.options().add("-H:Log=registerResource:3");
		assertThat(registry.options()).containsExactly("-H:+PrintAnalysisCallTree", "-H:Log=registerResource:3");
	}


	@SuppressWarnings("unused")
	public static class TestClass {

		private String field;

		void setName(String name) {

		}
	}

}
