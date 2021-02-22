package org.springframework.aot.factories;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aot.BuildContext;
import org.springframework.aot.CodeGenerationException;
import org.springframework.nativex.AotOptions;

/**
 * @author Brian Clozel
 */
class FactoriesCodeContributors {

	private final Log logger = LogFactory.getLog(FactoriesCodeContributors.class);

	private final List<FactoriesCodeContributor> contributors;

	FactoriesCodeContributors(AotOptions aotOptions) {
		this.contributors = Arrays.asList(new AutoConfigurationFactoriesCodeContributor(aotOptions),
				new NoArgConstructorFactoriesCodeContributor(),
				new PrivateFactoriesCodeContributor(),
				new DefaultFactoriesCodeContributor(aotOptions));
	}

	public CodeGenerator createCodeGenerator(List<SpringFactory> factories, BuildContext context, AotOptions aotOptions) {
		CodeGenerator codeGenerator = new CodeGenerator(aotOptions);
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
