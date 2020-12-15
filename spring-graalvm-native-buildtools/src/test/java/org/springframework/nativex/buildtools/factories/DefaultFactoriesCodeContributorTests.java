package org.springframework.nativex.buildtools.factories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.nativex.buildtools.TypeSystemExtension;
import org.springframework.nativex.buildtools.factories.fixtures.PublicFactory;
import org.springframework.nativex.buildtools.factories.fixtures.TestFactory;
import org.springframework.nativex.type.TypeSystem;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DefaultFactoriesCodeContributor}
 *
 * @author Brian Clozel
 */
@ExtendWith(TypeSystemExtension.class)
class DefaultFactoriesCodeContributorTests {

	DefaultFactoriesCodeContributor contributor = new DefaultFactoriesCodeContributor();

	@Test
	void shouldContributeWhenPublicConstructor(TypeSystem typeSystem) {
		SpringFactory factory = SpringFactory.resolve(TestFactory.class.getName(), PublicFactory.class.getName(), typeSystem);
		assertThat(this.contributor.canContribute(factory)).isTrue();
	}

	@Test
	void shouldNotContributeWhenPublicConstructorNotAvailable(TypeSystem typeSystem) throws Exception {
		Class<?> protectedClass = getClass().getClassLoader()
			.loadClass("org.springframework.nativex.buildtools.factories.fixtures.ProtectedFactory");
		SpringFactory factory = SpringFactory.resolve(TestFactory.class.getName(),
				"org.springframework.nativex.buildtools.factories.fixtures.ProtectedFactory", typeSystem);
		assertThat(this.contributor.canContribute(factory)).isFalse();
	}

	@Test
	void shouldContributeStaticStatement(TypeSystem typeSystem) {
		CodeGenerator code = new CodeGenerator();
		SpringFactory factory = SpringFactory.resolve(TestFactory.class.getName(), PublicFactory.class.getName(), typeSystem);
		this.contributor.contribute(factory, code);
		assertThat(code.generateStaticSpringFactories().toString())
				.contains("factories.add(org.springframework.nativex.buildtools.factories.fixtures.TestFactory.class, " +
						"() -> new org.springframework.nativex.buildtools.factories.fixtures.PublicFactory());\n");
	}

	@Test
	void shouldContributeStaticStatementForInnerClass(TypeSystem typeSystem) {
		CodeGenerator code = new CodeGenerator();
		SpringFactory factory = SpringFactory.resolve(TestFactory.class.getName(), PublicFactory.InnerFactory.class.getName(), typeSystem);
		this.contributor.contribute(factory, code);
		assertThat(code.generateStaticSpringFactories().toString())
				.contains("factories.add(org.springframework.nativex.buildtools.factories.fixtures.TestFactory.class, " +
						"() -> new org.springframework.nativex.buildtools.factories.fixtures.PublicFactory.InnerFactory());\n");
	}

}