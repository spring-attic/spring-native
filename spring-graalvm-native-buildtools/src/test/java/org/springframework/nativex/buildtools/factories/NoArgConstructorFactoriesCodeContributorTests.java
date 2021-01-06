package org.springframework.nativex.buildtools.factories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.nativex.buildtools.TypeSystemExtension;
import org.springframework.nativex.buildtools.factories.fixtures.DeprecatedConstructorFactory;
import org.springframework.nativex.buildtools.factories.fixtures.MissingDefaultConstructorFactory;
import org.springframework.nativex.buildtools.factories.fixtures.PublicFactory;
import org.springframework.nativex.buildtools.factories.fixtures.TestFactory;
import org.springframework.nativex.type.TypeSystem;

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
	void shouldContributeWhenDeprecatedDefaultConstructor(TypeSystem typeSystem) {
		SpringFactory factory = SpringFactory.resolve(TestFactory.class.getName(), DeprecatedConstructorFactory.class.getName(), typeSystem);
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
				"org.springframework.nativex.buildtools.factories.fixtures.ProtectedFactory", typeSystem);
		assertThat(this.contributor.canContribute(factory)).isFalse();
	}

	@Test
	void shouldContributeFactoryNames(TypeSystem typeSystem) {
		CodeGenerator code = new CodeGenerator();
		SpringFactory factory = SpringFactory.resolve(TestFactory.class.getName(), MissingDefaultConstructorFactory.class.getName(), typeSystem);
		this.contributor.contribute(factory, code);
		assertThat(code.generateStaticSpringFactories().toString())
				.contains("names.add(TestFactory.class, \"org.springframework.nativex.buildtools.factories.fixtures.MissingDefaultConstructorFactory\");");
	}

}