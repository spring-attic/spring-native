package org.springframework.nativex.buildtools.factories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.nativex.buildtools.TypeSystemExtension;
import org.springframework.nativex.buildtools.factories.fixtures.PublicFactory;
import org.springframework.nativex.buildtools.factories.fixtures.TestFactory;
import org.springframework.nativex.type.TypeSystem;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PrivateFactoriesCodeContributor}
 *
 * @author Brian Clozel
 */
@ExtendWith(TypeSystemExtension.class)
class PrivateFactoriesCodeContributorTests {

	PrivateFactoriesCodeContributor contributor = new PrivateFactoriesCodeContributor();

	@Test
	void shouldNotContributeWhenPublicConstructor(TypeSystem typeSystem) {
		SpringFactory factory = SpringFactory.resolve(TestFactory.class.getName(), PublicFactory.class.getName(), typeSystem);
		assertThat(this.contributor.canContribute(factory)).isFalse();
	}

	@Test
	void shouldContributeWhenClassIsProtected(TypeSystem typeSystem) throws Exception {
		SpringFactory factory = SpringFactory.resolve(TestFactory.class.getName(),
				"org.springframework.nativex.buildtools.factories.fixtures.ProtectedFactory", typeSystem);
		assertThat(this.contributor.canContribute(factory)).isTrue();
	}

	@Test
	void shouldContributeStaticStatement(TypeSystem typeSystem) throws Exception {
		CodeGenerator code = new CodeGenerator();
		SpringFactory factory = SpringFactory.resolve(TestFactory.class.getName(),
				"org.springframework.nativex.buildtools.factories.fixtures.ProtectedFactory", typeSystem);
		this.contributor.contribute(factory, code);
		assertThat(code.generateStaticSpringFactories().toString())
				.contains("factories.add(TestFactory.class, () -> _FactoryProvider.protectedFactory());\n");
		assertThat(code.generateStaticFactoryClasses()).hasSize(1);
		assertThat(code.generateStaticFactoryClasses().get(0).toString())
				.isEqualTo("package org.springframework.nativex.buildtools.factories.fixtures;\n" +
				"\n" +
				"public abstract class _FactoryProvider {\n" +
				"  public static ProtectedFactory protectedFactory() {\n" +
				"    return new ProtectedFactory();\n" +
				"  }\n" +
				"}\n");
	}

}