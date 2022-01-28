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

package org.springframework.aot.test.build;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Tests for {@link TestClassesFinder}
 *
 * @author Brian Clozel
 */
class TestClassesFinderTests {

	static String PACKAGE = "/org/springframework/aot/test/samples/tests/";

	static List<String> TEST_CLASSES = Arrays.asList("ExtendedWithSpring.class", "ExtendedWithTestWatcher.class",
			"SpringBootTestAnnotated.class", "SimpleClass.class", "MultipleExtendsWithSpring.class",
			"SpringWithNestedTest.class", "SpringWithNestedTest$NestedTest.class", "SpringWithNestedTest$NestedTest$NestedTest2.class");

	@TempDir
	static Path tempDirectory;

	@BeforeAll
	static void setupTestClasses() throws IOException {
		Path folder = tempDirectory.resolve("folder");
		Files.createDirectories(folder);
		for (String testClassName : TEST_CLASSES) {
			URL classResource = TestClassesFinderTests.class.getResource(PACKAGE + testClassName);
			try (InputStream is = classResource.openStream()) {
				Files.copy(is, folder.resolve(testClassName));
			}
		}
	}

	@Test
	void detectTestClasses() throws IOException {
		List<String> testClasses = TestClassesFinder.findTestClasses(tempDirectory);
		assertThat(testClasses).containsOnly("org.springframework.aot.test.samples.tests.ExtendedWithSpring",
				"org.springframework.aot.test.samples.tests.SpringBootTestAnnotated",
				"org.springframework.aot.test.samples.tests.MultipleExtendsWithSpring",
				"org.springframework.aot.test.samples.tests.SpringWithNestedTest",
				"org.springframework.aot.test.samples.tests.SpringWithNestedTest$NestedTest",
				"org.springframework.aot.test.samples.tests.SpringWithNestedTest$NestedTest$NestedTest2");
	}
}