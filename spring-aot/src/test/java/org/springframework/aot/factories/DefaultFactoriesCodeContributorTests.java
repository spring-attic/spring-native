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

package org.springframework.aot.factories;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.aot.build.context.BuildContext;
import org.springframework.aot.factories.fixtures.PublicFactory;
import org.springframework.aot.factories.fixtures.TestFactory;
import org.springframework.nativex.AotOptions;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DefaultFactoriesCodeContributor}
 *
 * @author Brian Clozel
 */
class DefaultFactoriesCodeContributorTests {

	DefaultFactoriesCodeContributor contributor = new DefaultFactoriesCodeContributor(new AotOptions());

	@Test
	void shouldContributeWhenPublicConstructor() {
		SpringFactory factory = SpringFactory.resolve(TestFactory.class.getName(), PublicFactory.class.getName(), DefaultFactoriesCodeContributorTests.class.getClassLoader());
		assertThat(this.contributor.canContribute(factory)).isTrue();
	}

	@Test
	void shouldNotContributeWhenPublicConstructorNotAvailable() throws Exception {
		Class<?> protectedClass = getClass().getClassLoader()
			.loadClass("org.springframework.aot.factories.fixtures.ProtectedFactory");
		SpringFactory factory = SpringFactory.resolve(TestFactory.class.getName(),
				"org.springframework.aot.factories.fixtures.ProtectedFactory", DefaultFactoriesCodeContributorTests.class.getClassLoader());
		assertThat(this.contributor.canContribute(factory)).isFalse();
	}

	@Test
	void shouldContributeStaticStatement() {
		CodeGenerator code = new CodeGenerator(new AotOptions());
		SpringFactory factory = SpringFactory.resolve(TestFactory.class.getName(), PublicFactory.class.getName(), DefaultFactoriesCodeContributorTests.class.getClassLoader());
		this.contributor.contribute(factory, code, Mockito.mock(BuildContext.class));
		assertThat(code.generateStaticSpringFactories().toString())
				.contains("factories.add(org.springframework.aot.factories.fixtures.TestFactory.class, " +
						"() -> new org.springframework.aot.factories.fixtures.PublicFactory());\n");
	}

	@Test
	void shouldContributeStaticStatementForInnerClass() {
		CodeGenerator code = new CodeGenerator(new AotOptions());
		SpringFactory factory = SpringFactory.resolve(TestFactory.class.getName(), PublicFactory.InnerFactory.class.getName(), DefaultFactoriesCodeContributorTests.class.getClassLoader());
		this.contributor.contribute(factory, code, Mockito.mock(BuildContext.class));
		assertThat(code.generateStaticSpringFactories().toString())
				.contains("factories.add(org.springframework.aot.factories.fixtures.TestFactory.class, " +
						"() -> new org.springframework.aot.factories.fixtures.PublicFactory.InnerFactory());\n");
	}

}