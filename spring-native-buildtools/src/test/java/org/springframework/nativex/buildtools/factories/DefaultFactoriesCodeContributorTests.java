package org.springframework.nativex.buildtools.factories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import org.springframework.core.type.classreading.TypeSystem;
import org.springframework.nativex.buildtools.BuildContext;
import org.springframework.nativex.buildtools.TypeSystemExtension;
import org.springframework.nativex.buildtools.factories.fixtures.PublicFactory;
import org.springframework.nativex.buildtools.factories.fixtures.TestFactory;

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
		this.contributor.contribute(factory, code, Mockito.mock(BuildContext.class));
		assertThat(code.generateStaticSpringFactories().toString())
				.contains("factories.add(org.springframework.nativex.buildtools.factories.fixtures.TestFactory.class, " +
						"org.springframework.nativex.buildtools.factories.fixtures.PublicFactory::new);\n");
	}

	@Test
	void shouldContributeStaticStatementForInnerClass(TypeSystem typeSystem) {
		CodeGenerator code = new CodeGenerator();
		SpringFactory factory = SpringFactory.resolve(TestFactory.class.getName(), PublicFactory.InnerFactory.class.getName(), typeSystem);
		this.contributor.contribute(factory, code, Mockito.mock(BuildContext.class));
		assertThat(code.generateStaticSpringFactories().toString())
				.contains("factories.add(org.springframework.nativex.buildtools.factories.fixtures.TestFactory.class, " +
						"org.springframework.nativex.buildtools.factories.fixtures.PublicFactory.InnerFactory::new);\n");
	}

}