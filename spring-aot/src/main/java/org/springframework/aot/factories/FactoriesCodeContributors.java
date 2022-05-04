/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.aot.factories;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aot.build.context.BuildContext;
import org.springframework.aot.build.CodeGenerationException;
import org.springframework.nativex.AotOptions;

/**
 * @author Brian Clozel
 */
class FactoriesCodeContributors {

	private final Log logger = LogFactory.getLog(FactoriesCodeContributors.class);

	private final List<FactoriesCodeContributor> contributors;

	FactoriesCodeContributors(AotOptions aotOptions) {
		this.contributors = Arrays.asList(new IgnoredFactoriesCodeContributor(),
				new TestExecutionListenerFactoriesCodeContributor(),
				new TestAutoConfigurationFactoriesCodeContributor(aotOptions),
				new FailureAnalyzersCodeContributor(),
				new NoArgConstructorFactoriesCodeContributor(),
				new PrivateFactoriesCodeContributor(),
				new DefaultFactoriesCodeContributor(aotOptions));
	}

	public CodeGenerator createCodeGenerator(Set<SpringFactory> factories, BuildContext context, AotOptions aotOptions) {
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
