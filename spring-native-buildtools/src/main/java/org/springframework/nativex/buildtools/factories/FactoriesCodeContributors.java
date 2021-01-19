package org.springframework.nativex.buildtools.factories;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.nativex.buildtools.BuildContext;
import org.springframework.nativex.buildtools.CodeGenerationException;

/**
 * @author Brian Clozel
 */
class FactoriesCodeContributors {

	private final Log logger = LogFactory.getLog(FactoriesCodeContributors.class);

	private final List<FactoriesCodeContributor> contributors;

	FactoriesCodeContributors() {
		this.contributors = Arrays.asList(new AutoConfigurationFactoriesCodeContributor(),
				new NoArgConstructorFactoriesCodeContributor(),
				new PrivateFactoriesCodeContributor(),
				new DefaultFactoriesCodeContributor());
	}

	public CodeGenerator createCodeGenerator(List<SpringFactory> factories, BuildContext context) {
		CodeGenerator codeGenerator = new CodeGenerator();
		for (SpringFactory factory : factories) {
			FactoriesCodeContributor contributor = this.contributors.stream()
					.filter(c -> c.canContribute(factory))
					.findFirst()
					.orElseThrow(() -> new CodeGenerationException("Cannot find contributor for factory: " + factory));
			contributor.contribute(factory, codeGenerator, context);
		}
		return codeGenerator;
	}

}
