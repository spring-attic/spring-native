package org.springframework.nativex.buildtools.factories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.nativex.buildtools.TypeSystemExtension;
import org.springframework.nativex.buildtools.factories.fixtures.PublicFactory;
import org.springframework.nativex.buildtools.factories.fixtures.TestAutoConfiguration;
import org.springframework.nativex.buildtools.factories.fixtures.TestFactory;
import org.springframework.nativex.type.TypeSystem;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AutoConfigurationFactoriesCodeContributor}
 *
 * @author Brian Clozel
 */
@ExtendWith(TypeSystemExtension.class)
class AutoConfigurationFactoriesCodeContributorTests {

	AutoConfigurationFactoriesCodeContributor contributor = new AutoConfigurationFactoriesCodeContributor();

	@Test
	void shouldContributeWhenAutoConfiguration(TypeSystem typeSystem) {
		SpringFactory factory = SpringFactory.resolve(EnableAutoConfiguration.class.getName(), PublicFactory.class.getName(), typeSystem);
		assertThat(this.contributor.canContribute(factory)).isTrue();
	}

	@Test
	void shouldNotContributeWhenNotAutoConfiguration(TypeSystem typeSystem) {
		SpringFactory factory = SpringFactory.resolve(TestFactory.class.getName(), PublicFactory.class.getName(), typeSystem);
		assertThat(this.contributor.canContribute(factory)).isFalse();
	}

	@Test
	void shouldContributeFactoryNames(TypeSystem typeSystem) {
		CodeGenerator code = new CodeGenerator();
		SpringFactory factory = SpringFactory.resolve(EnableAutoConfiguration.class.getName(), PublicFactory.class.getName(), typeSystem);
		this.contributor.contribute(factory, code);
		assertThat(code.generateStaticSpringFactories().toString())
				.contains("names.add(EnableAutoConfiguration.class, \"org.springframework.nativex.buildtools.factories.fixtures.PublicFactory\");");
	}

	@Test
	void shouldContributeFactoryNamesWhenConditionMet(TypeSystem typeSystem) {
		CodeGenerator code = new CodeGenerator();
		SpringFactory factory = SpringFactory.resolve(EnableAutoConfiguration.class.getName(), TestAutoConfiguration.class.getName(), typeSystem);
		this.contributor.contribute(factory, code);
		assertThat(code.generateStaticSpringFactories().toString())
				.contains("names.add(EnableAutoConfiguration.class, \"org.springframework.nativex.buildtools.factories.fixtures.TestAutoConfiguration\");");
	}

}