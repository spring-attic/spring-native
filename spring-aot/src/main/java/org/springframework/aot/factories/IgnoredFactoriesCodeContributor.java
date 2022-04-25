/*
 * Copyright 2019-2022 the original author or authors.
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
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aot.build.BootstrapCodeGenerator;
import org.springframework.aot.build.context.BuildContext;

/**
 * {@link FactoriesCodeContributor} ignoring Spring Factories that are now handled
 * during the bootstrap code generation process.
 * @see BootstrapCodeGenerator
 */
class IgnoredFactoriesCodeContributor implements FactoriesCodeContributor {

	private static final Predicate<SpringFactory> IGNORED_FACTORY_PACKAGES = factoriesInPackages(
			"org.springframework.aot.context.bootstrap",
			"org.springframework.aot.context.origin",
			"org.springframework.aot.test.context.bootstrap");

	private static final Predicate<SpringFactory> IGNORED_FACTORY_TYPES = factoryTypes(
			"org.springframework.boot.ApplicationContextFactory",
			"org.springframework.boot.autoconfigure.AutoConfigurationImportListener",
			"org.springframework.boot.autoconfigure.AutoConfigurationImportFilter",
			"org.springframework.boot.autoconfigure.EnableAutoConfiguration",
			"org.springframework.context.annotation.BeanDefinitionPostProcessor",
			"org.springframework.nativex.type.NativeConfiguration",
			"org.springframework.aot.build.BootstrapContributor");

	private static final Predicate<SpringFactory> CONTEXT_CUSTOMIZER_FACTORY = factoryEntry(
			"org.springframework.test.context.ContextCustomizerFactory",
			"org.springframework.boot.test.autoconfigure.OverrideAutoConfigurationContextCustomizerFactory",
			"org.springframework.boot.test.autoconfigure.filter.TypeExcludeFiltersContextCustomizerFactory",
			"org.springframework.boot.test.context.ImportsContextCustomizerFactory");

	private static final Predicate<SpringFactory> IGNORED_TEST_EXECUTION_LISTENERS = factoryEntry(
			"org.springframework.test.context.TestExecutionListener",
			"org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener",
			"org.springframework.boot.test.mock.mockito.ResetMocksTestExecutionListener"
	);

	private final Log logger = LogFactory.getLog(IgnoredFactoriesCodeContributor.class);

	@Override
	public boolean canContribute(SpringFactory factory) {
		return IGNORED_FACTORY_PACKAGES
				.or(IGNORED_FACTORY_TYPES)
				.or(CONTEXT_CUSTOMIZER_FACTORY)
				.or(IGNORED_TEST_EXECUTION_LISTENERS)
				.test(factory);
	}

	@Override
	public void contribute(SpringFactory factory, CodeGenerator code, BuildContext context) {
		// No-op, ignored.
		logger.debug("Skip build time factory Type:" + factory.getFactory().getName());
	}

	private static Predicate<SpringFactory> factoryTypes(String... factoryTypes) {
		Set<String> candidates = new HashSet<>(Arrays.asList(factoryTypes));
		return (springFactory) -> candidates.contains(springFactory.getFactoryType().getName());
	}

	private static Predicate<SpringFactory> factoriesInPackages(String... packageNames) {
		return (springFactory) -> {
			for (String packageName : packageNames) {
				if (springFactory.getFactoryType().getName().startsWith(packageName)) {
					return true;
				}
			}
			return false;
		};
	}

	private static Predicate<SpringFactory> factoryEntry(String factoryType, String... factoryImplementations) {
		Set<String> candidateImplementations = new HashSet<>(Arrays.asList(factoryImplementations));
		return factoryTypes(factoryType).and((springFactory) ->
				candidateImplementations.contains(springFactory.getFactory().getName()));
	}

}
