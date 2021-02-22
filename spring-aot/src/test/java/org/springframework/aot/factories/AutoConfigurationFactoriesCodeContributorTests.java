package org.springframework.aot.factories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import org.springframework.aot.BuildContext;
import org.springframework.aot.TypeSystemExtension;
import org.springframework.aot.factories.fixtures.PublicFactory;
import org.springframework.aot.factories.fixtures.TestAutoConfiguration;
import org.springframework.aot.factories.fixtures.TestAutoConfigurationMissingType;
import org.springframework.aot.factories.fixtures.TestFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.core.type.classreading.TypeSystem;
import org.springframework.nativex.AotOptions;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AutoConfigurationFactoriesCodeContributor}
 *
 * @author Brian Clozel
 */
@ExtendWith(TypeSystemExtension.class)
class AutoConfigurationFactoriesCodeContributorTests {

	FactoriesCodeContributor contributor = new AutoConfigurationFactoriesCodeContributor(new AotOptions());

	BuildContext buildContext = Mockito.mock(BuildContext.class);

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
		CodeGenerator code = new CodeGenerator(new AotOptions());
		SpringFactory factory = SpringFactory.resolve(EnableAutoConfiguration.class.getName(), PublicFactory.class.getName(), typeSystem);
		Mockito.when(buildContext.getTypeSystem()).thenReturn(typeSystem);
		this.contributor.contribute(factory, code, this.buildContext);
		assertThat(code.generateStaticSpringFactories().toString())
				.contains("names.add(EnableAutoConfiguration.class, \"org.springframework.aot.factories.fixtures.PublicFactory\");");
	}

	@Test
	void shouldContributeFactoryNamesWhenConditionMet(TypeSystem typeSystem) {
		CodeGenerator code = new CodeGenerator(new AotOptions());
		SpringFactory factory = SpringFactory.resolve(EnableAutoConfiguration.class.getName(), TestAutoConfiguration.class.getName(), typeSystem);
		Mockito.when(buildContext.getTypeSystem()).thenReturn(typeSystem);
		this.contributor.contribute(factory, code, this.buildContext);
		assertThat(code.generateStaticSpringFactories().toString())
				.contains("names.add(EnableAutoConfiguration.class, \"org.springframework.aot.factories.fixtures.TestAutoConfiguration\");");
	}

	@Test
	void shouldContributeFactoryNamesWhenConditionNotMet(TypeSystem typeSystem) {
		CodeGenerator code = new CodeGenerator(new AotOptions());
		SpringFactory factory = SpringFactory.resolve(EnableAutoConfiguration.class.getName(), TestAutoConfigurationMissingType.class.getName(), typeSystem);
		Mockito.when(buildContext.getTypeSystem()).thenReturn(typeSystem);
		this.contributor.contribute(factory, code, this.buildContext);
		assertThat(code.generateStaticSpringFactories().toString())
				.doesNotContain("names.add(EnableAutoConfiguration.class, \"org.springframework.aot.factories.fixtures.TestAutoConfigurationMissingType\");");
	}

}