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

package com.example;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

/**
 * Companion for {@link ImportBarTests}.
 *
 * <p>These two test classes share a common {@link Config} class, but each
 * imports a different local component. Thus, if the AOT context caching support
 * is not working correctly with regard to {@link Import @Import}, then only
 * one {@code ApplicationContext} would be loaded for both classes and one of the
 * test classes would fail.
 *
 * @author Sam Brannen
 */
@SpringBootTest(classes = Config.class)
@Import(ImportFooTests.Foo.class)
class ImportFooTests {

	@Autowired
	Foo foo;

	@Autowired
	Integer number;

	@Test
	void test() {
		assertThat(foo).isNotNull();
		assertThat(number).isEqualTo(42);
	}

	@Component
	static class Foo {
	}

}
