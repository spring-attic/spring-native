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

package org.springframework.aot.test.boot;

import org.springframework.aot.test.context.bootstrap.generator.AotTestContextProcessor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContextBootstrapper;

/**
 * An {@link AotTestContextProcessor} that handles Spring Boot test. This includes support
 * for {@link SpringBootTest} as well as slice tests.
 *
 * @author Stephane Nicoll
 */
@Order(0)
class SpringBootAotTestContextProcessor implements AotTestContextProcessor {

	private final BuildTimeSpringBootContextLoader contextLoader = new BuildTimeSpringBootContextLoader();

	@Override
	public boolean supports(TestContextBootstrapper bootstrapper) {
		return bootstrapper instanceof SpringBootTestContextBootstrapper;
	}

	@Override
	public GenericApplicationContext prepareTestContext(MergedContextConfiguration config) {
		try {
			return this.contextLoader.loadContext(config);
		}
		catch (Exception ex) {
			throw new IllegalStateException("Failed to prepare test context using " + config, ex);
		}
	}

}
