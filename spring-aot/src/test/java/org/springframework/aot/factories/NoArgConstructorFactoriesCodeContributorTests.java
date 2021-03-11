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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import org.springframework.aot.BuildContext;
import org.springframework.aot.TypeSystemExtension;
import org.springframework.aot.factories.fixtures.MissingDefaultConstructorFactory;
import org.springframework.aot.factories.fixtures.PublicFactory;
import org.springframework.aot.factories.fixtures.TestFactory;
import org.springframework.core.type.classreading.TypeSystem;
import org.springframework.nativex.AotOptions;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link NoArgConstructorFactoriesCodeContributor}
 *
 * @author Brian Clozel
 */
@ExtendWith(TypeSystemExtension.class)
class NoArgConstructorFactoriesCodeContributorTests {

	NoArgConstructorFactoriesCodeContributor contributor = new NoArgConstructorFactoriesCodeContributor();

	@Test
	void shouldContributeWhenMissingDefaultConstructor(TypeSystem typeSystem) {
		SpringFactory factory = SpringFactory.resolve(TestFactory.class.getName(), MissingDefaultConstructorFactory.class.getName(), typeSystem);
		assertThat(this.contributor.canContribute(factory)).isTrue();
	}

	@Test
	void shouldNotContributeWhenDefaultDefaultConstructorPresent(TypeSystem typeSystem) {
		SpringFactory factory = SpringFactory.resolve(TestFactory.class.getName(), PublicFactory.class.getName(), typeSystem);
		assertThat(this.contributor.canContribute(factory)).isFalse();
	}

	@Test
	void shouldNotContributeWhenDefaultProtectedConstructorPresent(TypeSystem typeSystem) {
		SpringFactory factory = SpringFactory.resolve(TestFactory.class.getName(),
				"org.springframework.aot.factories.fixtures.ProtectedFactory", typeSystem);
		assertThat(this.contributor.canContribute(factory)).isFalse();
	}

	@Test
	void shouldContributeFactoryNames(TypeSystem typeSystem) {
		CodeGenerator code = new CodeGenerator(new AotOptions());
		SpringFactory factory = SpringFactory.resolve(TestFactory.class.getName(), MissingDefaultConstructorFactory.class.getName(), typeSystem);
		this.contributor.contribute(factory, code, Mockito.mock(BuildContext.class));
		assertThat(code.generateStaticSpringFactories().toString())
				.contains("names.add(TestFactory.class, \"org.springframework.aot.factories.fixtures.MissingDefaultConstructorFactory\");");
	}

}