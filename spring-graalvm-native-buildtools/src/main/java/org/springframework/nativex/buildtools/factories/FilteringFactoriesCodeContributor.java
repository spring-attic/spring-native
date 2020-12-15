package org.springframework.nativex.buildtools.factories;

import java.util.Arrays;
import java.util.List;

/**
 * {@link FactoriesCodeContributor} that avoids instantiating a selection of Spring Factories.
 *
 * @author Brian Clozel
 */
class FilteringFactoriesCodeContributor implements FactoriesCodeContributor {

	private static final List<String> IGNORED_FACTORIES = Arrays.asList(
			// Handled with a substitution in Target_SpringApplication
			"org.springframework.boot.SpringApplicationRunListener",
			"org.springframework.boot.SpringBootExceptionReporter"
	);

	@Override
	public boolean canContribute(SpringFactory factory) {
		return IGNORED_FACTORIES.contains(factory.getFactoryType().getDottedName());
	}

	@Override
	public void contribute(SpringFactory factory, CodeGenerator code) {
		// ignoring this factory
	}
}
