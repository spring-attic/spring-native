package org.springframework.nativex.buildtools.factories;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.nativex.buildtools.TypeSystemExtension;
import org.springframework.nativex.type.TypeSystem;
import org.springframework.util.StreamUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FilteringFactoriesCodeContributor}
 *
 * @author Brian Clozel
 */
@ExtendWith(TypeSystemExtension.class)
class FilteringFactoriesCodeContributorTests {

	FilteringFactoriesCodeContributor contributor = new FilteringFactoriesCodeContributor();

	@Test
	void shouldContributeToFilteredFactories(TypeSystem typeSystem) {
		SpringFactory factory = SpringFactory.resolve("org.springframework.boot.SpringApplicationRunListener", FilteringFactoriesCodeContributor.class.getName(), typeSystem);
		assertThat(this.contributor.canContribute(factory)).isTrue();
	}

	@Test
	void shouldNotWriteAnything(TypeSystem typeSystem) throws Exception {
		SpringFactory factory = SpringFactory.resolve("org.springframework.boot.SpringApplicationRunListener", FilteringFactoriesCodeContributor.class.getName(), typeSystem);
		CodeGenerator code = new CodeGenerator();
		this.contributor.contribute(factory, code);
		assertThat(code.generateStaticFactoryClasses()).isEmpty();
		assertThat(code.generateStaticSpringFactories().toString()).isEqualTo(expectedJavaSource("emptyStaticSpringFactories"));
	}

	String expectedJavaSource(String name) throws Exception {
		InputStream input = getClass().getResourceAsStream(name + ".txt");
		return StreamUtils.copyToString(input, StandardCharsets.UTF_8);
	}

}