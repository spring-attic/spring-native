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

package org.springframework.aot.test.context.support;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import org.springframework.aot.test.context.bootstrap.generator.AotTestContextProcessor;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContextBootstrapper;
import org.springframework.test.context.support.DefaultTestContextBootstrapper;

/**
 * An {@link AotTestContextProcessor} that handles the default test context bootstrapper
 * from the core Test Context Framework. This includes support for test annotated with
 * {@link ContextConfiguration}.
 *
 * @author Stephane Nicoll
 */
@Order(Ordered.LOWEST_PRECEDENCE)
class DefaultAotTestContextProcessor implements AotTestContextProcessor {

	private final DefaultBuildTimeConfigContextLoader contextLoader = new DefaultBuildTimeConfigContextLoader();

	@Override
	public boolean supports(TestContextBootstrapper bootstrapper) {
		return bootstrapper instanceof DefaultTestContextBootstrapper;
	}

	@Override
	public GenericApplicationContext prepareTestContext(MergedContextConfiguration config) {
		try {
			return (GenericApplicationContext) this.contextLoader.loadContext(config);
		}
		catch (Exception ex) {
			throw new IllegalStateException("Failed to prepare test context using " + config, ex);
		}
	}

	@Override
	public CodeBlock writeInstanceSupplier(MergedContextConfiguration config, ClassName applicationContextInitializer) {
		return CodeBlock.of("() -> new $T($T.class)", AotDefaultConfigContextLoader.class, applicationContextInitializer);
	}

}
