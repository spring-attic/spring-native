/*
 * Copyright 2002-2021 the original author or authors.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aot.BuildContext;

/**
 * {@link FactoriesCodeContributor} ignoring Spring Factories that are now handled
 * during the bootstrap code generation process.
 * @see org.springframework.aot.BootstrapCodeGenerator
 */
public class IgnoredFactoriesCodeContributor implements FactoriesCodeContributor {

	private static List<String> IGNORED_FACTORY_PACKAGES = Arrays.asList(
			"org.springframework.context.bootstrap", "org.springframework.context.origin");

	private static List<String> IGNORED_FACTORY_TYPES = Arrays.asList(
			"org.springframework.boot.autoconfigure.AutoConfigurationImportListener",
			"org.springframework.boot.autoconfigure.AutoConfigurationImportFilter",
			"org.springframework.boot.autoconfigure.EnableAutoConfiguration");

	private final Log logger = LogFactory.getLog(IgnoredFactoriesCodeContributor.class);

	@Override
	public boolean canContribute(SpringFactory factory) {
		for (String factoryPackage : IGNORED_FACTORY_PACKAGES) {
			if (factory.getFactoryType().getClassName().startsWith(factoryPackage)) {
				return true;
			}
		}
		return IGNORED_FACTORY_TYPES.contains(factory.getFactoryType().getClassName());
	}

	@Override
	public void contribute(SpringFactory factory, CodeGenerator code, BuildContext context) {
		// No-op, ignored.
		logger.debug("Skip build time factory Type:" + factory.getFactory().getClassName());
	}
}
