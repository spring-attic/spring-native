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

package org.springframework.aot.test.context.bootstrap.generator;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.aot.test.context.bootstrap.generator.test.ContextBootstrapStructure;
import org.springframework.aot.test.context.bootstrap.generator.test.TestContextAotProcessorTester;
import org.springframework.aot.test.samples.app.SampleApplicationAnotherTests;
import org.springframework.aot.test.samples.app.SampleApplicationTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TestContextAotProcessor}.
 *
 * @author Stephane Nicoll
 */
class TestContextAotProcessorTests {

	private TestContextAotProcessorTester tester;

	@BeforeEach
	void setup(@TempDir Path directory) {
		this.tester = new TestContextAotProcessorTester(directory);
	}

	@Test
	void processSingleTestClass() {
		ContextBootstrapStructure structure = this.tester.process(SampleApplicationTests.class);
		assertThat(structure).contextBootstrapInitializer("SampleApplicationTestsContextInitializer").lines()
				.containsSubsequence(
						"/**",
						" * AOT generated context for {@code SampleApplicationTests}.",
						" */")
				.containsSubsequence(
						"public class SampleApplicationTestsContextInitializer implements ApplicationContextInitializer<GenericApplicationContext> {",
						"  @Override",
						"  public void initialize(GenericApplicationContext context) {").containsSubsequence(
						"  }",
						"}");
	}

	@Test
	void processSeveralTestClassesSharingTheSameConfiguration() {
		ContextBootstrapStructure structure = this.tester.process(
				SampleApplicationTests.class, SampleApplicationAnotherTests.class);
		assertThat(structure).contextBootstrapInitializer("TestContextBootstrapInitializer0").lines()
				.containsSubsequence(
						"/**",
						" * AOT generated context for {@code SampleApplicationTests} and {@code SampleApplicationAnotherTests}.",
						" */")
				.containsSubsequence(
						"public class TestContextBootstrapInitializer0 implements ApplicationContextInitializer<GenericApplicationContext> {",
						"  @Override",
						"  public void initialize(GenericApplicationContext context) {").containsSubsequence(
						"  }",
						"}");
	}

	@Test
	void processWritesContextLoaderMappingAtStandardLocation() {
		ContextBootstrapStructure structure = this.tester.process(
				SampleApplicationTests.class, SampleApplicationAnotherTests.class);
		assertThat(structure).contextBootstrapInitializer("TestContextBootstrapInitializer")
				.removeIndent(1).lines().containsSubsequence(
						"public static Map<String, Supplier<SmartContextLoader>> getContextLoaders() {",
						"  Map<String, Supplier<SmartContextLoader>> entries = new HashMap<>();",
						"  entries.put(\"org.springframework.aot.test.samples.app.SampleApplicationTests\", () -> new SpringBootAotContextLoader(TestContextBootstrapInitializer0.class));",
						"  entries.put(\"org.springframework.aot.test.samples.app.SampleApplicationAnotherTests\", () -> new SpringBootAotContextLoader(TestContextBootstrapInitializer0.class));",
						"  return entries;",
						"}");
	}

	@Test
	void processInvokeTestNativeConfigurationRegistrar() {
		ContextBootstrapStructure structure = this.tester.process(SampleApplicationTests.class);
		assertThat(structure).hasResourcePattern("org/springframework/aot/test/samples/app/SampleApplication.class");
	}

}
