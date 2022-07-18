/*
 * Copyright 2002-2021 the original author or authors.
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

package org.springframework.aot.build;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.aot.build.context.BuildContext;
import org.springframework.aot.build.context.ResourceFile;
import org.springframework.aot.build.context.ResourceFiles;
import org.springframework.aot.build.context.SourceFiles;
import org.springframework.nativex.AotOptions;
import org.springframework.nativex.domain.proxies.JdkProxyDescriptor;
import org.springframework.nativex.domain.reflect.ClassDescriptor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BootstrapCodeGenerator}.
 * @author Brian Clozel
 */
class BootstrapCodeGeneratorTests {

	@Test
	void shouldWriteSourceFiles(@TempDir File tempDir) throws IOException {
		BootstrapCodeGenerator codeGenerator = createCodeGenerator(new SourceFilesContributor());
		ApplicationStructure applicationStructure = getApplicationStructure(tempDir);

		codeGenerator.generate(AotPhase.MAIN, applicationStructure);

		Path sourcesPath = applicationStructure.getSourcesPath();
		assertThat(sourcesPath).exists();
		Path pathToTestClass = Paths.get("io", "spring", "test", "TestClass.java");
		assertThat(sourcesPath.resolve(pathToTestClass)).exists();
	}

	@Test
	void shouldWriteResourceFiles(@TempDir File tempDir) throws IOException {
		BootstrapCodeGenerator codeGenerator = createCodeGenerator(new ResourceFilesContributor(tempDir.toPath()));
		ApplicationStructure applicationStructure = getApplicationStructure(tempDir);

		codeGenerator.generate(AotPhase.MAIN, applicationStructure);

		Path resourcesPath = applicationStructure.getResourcesPath();
		assertThat(resourcesPath).exists();
		Path pathToTestResource = Paths.get("src", "main", "resources", "test.txt");
		assertThat(resourcesPath.resolve(pathToTestResource)).exists();
	}

	@Test
	void shouldWriteReflectionConfig(@TempDir File tempDir) throws IOException {
		BootstrapCodeGenerator codeGenerator = createCodeGenerator(new ReflectionContributor());
		ApplicationStructure applicationStructure = getApplicationStructure(tempDir);

		codeGenerator.generate(AotPhase.MAIN, applicationStructure);

		Path configPath = getGraalVMConfigPath(applicationStructure, "reflect-config.json");
		assertThat(configPath).exists();

		DocumentContext document = JsonPath.parse(Files.readString(configPath));
		String[] values = document.read(JsonPath.compile("$.[*].name"), String[].class);
		assertThat(values).containsOnly(BootstrapCodeGenerator.class.getCanonicalName());
	}

	@Test
	void shouldWriteProxiesConfig(@TempDir File tempDir) throws IOException {
		BootstrapCodeGenerator codeGenerator = createCodeGenerator(new ProxiesContributor());
		ApplicationStructure applicationStructure = getApplicationStructure(tempDir);

		codeGenerator.generate(AotPhase.MAIN, applicationStructure);

		Path configPath = getGraalVMConfigPath(applicationStructure, "proxy-config.json");
		assertThat(configPath).exists();

		DocumentContext document = JsonPath.parse(Files.readString(configPath));
		String[] values = document.read(JsonPath.compile("$.[*].[*]"), String[].class);
		assertThat(values).containsOnly(BootstrapCodeGenerator.class.getCanonicalName());
	}

	@Test
	void shouldWriteResourcesConfig(@TempDir File tempDir) throws IOException {
		BootstrapCodeGenerator codeGenerator = createCodeGenerator(new ResourcesContributor());
		ApplicationStructure applicationStructure = getApplicationStructure(tempDir);

		codeGenerator.generate(AotPhase.MAIN, applicationStructure);

		Path configPath = getGraalVMConfigPath(applicationStructure, "resource-config.json");
		assertThat(configPath).exists();

		DocumentContext document = JsonPath.parse(Files.readString(configPath));
		String[] values = document.read(JsonPath.compile("$.resources.includes[*].pattern"), String[].class);
		assertThat(values).containsOnly("sample/*.txt");
	}

	@Test
	void shouldWriteSerializationConfig(@TempDir File tempDir) throws IOException {
		BootstrapCodeGenerator codeGenerator = createCodeGenerator(new SerializationContributor());
		ApplicationStructure applicationStructure = getApplicationStructure(tempDir);

		codeGenerator.generate(AotPhase.MAIN, applicationStructure);

		Path proxyConfigPath = getGraalVMConfigPath(applicationStructure, "serialization-config.json");
		assertThat(proxyConfigPath).exists();

		DocumentContext document = JsonPath.parse(Files.readString(proxyConfigPath));
		String[] values = document.read(JsonPath.compile("$.[*].name"), String[].class);
		assertThat(values).containsOnly(BootstrapCodeGenerator.class.getCanonicalName());
	}

	@Test
	void shouldWriteJniConfig(@TempDir File tempDir) throws IOException {
		BootstrapCodeGenerator codeGenerator = createCodeGenerator(new JniContributor());
		ApplicationStructure applicationStructure = getApplicationStructure(tempDir);

		codeGenerator.generate(AotPhase.MAIN, applicationStructure);

		Path proxyConfigPath = getGraalVMConfigPath(applicationStructure, "jni-config.json");
		assertThat(proxyConfigPath).exists();

		DocumentContext document = JsonPath.parse(Files.readString(proxyConfigPath));
		String[] values = document.read(JsonPath.compile("$.[*].name"), String[].class);
		assertThat(values).containsOnly(BootstrapCodeGenerator.class.getCanonicalName());
	}

	private Path getGraalVMConfigPath(ApplicationStructure applicationStructure, String configFileName) {
		Path graalVMConfigPath = applicationStructure.getResourcesPath().resolve(ResourceFile.NATIVE_CONFIG_PATH);
		return graalVMConfigPath.resolve(Paths.get(configFileName));
	}

	private ApplicationStructure getApplicationStructure(File tempDir) throws IOException {
		return new ApplicationStructure(tempDir.toPath().resolve("sources"), tempDir.toPath().resolve("resources"),
				Collections.emptySet(), Collections.emptyList(), "", "",
				Collections.emptyList(), Collections.emptyList(), Thread.currentThread().getContextClassLoader(), Collections.emptyList());
	}

	private BootstrapCodeGenerator createCodeGenerator(BootstrapContributor... contributors) {
		return new BootstrapCodeGenerator(new AotOptions(), Arrays.asList(contributors));
	}

	static class SourceFilesContributor implements BootstrapContributor {

		@Override
		public void contribute(BuildContext context, AotOptions aotOptions) {
			context.addSourceFiles(SourceFiles.fromStaticFile("io.spring.test", "TestClass",
					new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8))));
		}
	}

	static class ResourceFilesContributor implements BootstrapContributor {

		final Path testResourcePath;

		ResourceFilesContributor(Path tempDirPath) throws IOException {
			this.testResourcePath = tempDirPath.resolve("test.txt");
			Files.write(this.testResourcePath, "test".getBytes(StandardCharsets.UTF_8));
		}

		@Override
		public void contribute(BuildContext context, AotOptions aotOptions) {
			context.addResources(ResourceFiles.fromStaticFile(this.testResourcePath, Paths.get("src", "main", "resources")));
		}
	}

	static class ReflectionContributor implements BootstrapContributor {
		@Override
		public void contribute(BuildContext context, AotOptions aotOptions) {
			context.describeReflection(consumer ->
					consumer.add(ClassDescriptor.of(BootstrapCodeGenerator.class.getCanonicalName())));
		}
	}

	static class ProxiesContributor implements BootstrapContributor {
		@Override
		public void contribute(BuildContext context, AotOptions aotOptions) {
			context.describeProxies(consumer ->
					consumer.add(JdkProxyDescriptor.of(Collections.singletonList(BootstrapCodeGenerator.class.getCanonicalName()))));
		}
	}

	static class ResourcesContributor implements BootstrapContributor {
		@Override
		public void contribute(BuildContext context, AotOptions aotOptions) {
			context.describeResources(consumer -> consumer.add("sample/*.txt"));
		}
	}

	static class SerializationContributor implements BootstrapContributor {
		@Override
		public void contribute(BuildContext context, AotOptions aotOptions) {
			context.describeSerialization(consumer -> consumer.add(BootstrapCodeGenerator.class.getCanonicalName()));
		}
	}

	static class JniContributor implements BootstrapContributor {
		@Override
		public void contribute(BuildContext context, AotOptions aotOptions) {
			context.describeJNIReflection(consumer ->
					consumer.add(ClassDescriptor.of(BootstrapCodeGenerator.class.getCanonicalName())));
		}
	}

}