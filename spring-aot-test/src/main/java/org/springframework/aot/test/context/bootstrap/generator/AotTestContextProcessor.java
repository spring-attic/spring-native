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

package org.springframework.aot.test.context.bootstrap.generator;

import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContextBootstrapper;

/**
 * Process a test context at build time so that it can be used as an input for code
 * generation.
 *
 * @author Stephane Nicoll
 */
public interface AotTestContextProcessor {

	/**
	 * Specify whether this instance supports the specified {@link TestContextBootstrapper}.
	 * @param bootstrapper the test context bootstrapper to check
	 * @return {@code true} if it supports it, {@code false} otherwise
	 */
	boolean supports(TestContextBootstrapper bootstrapper);

	/**
	 * Prepare a {@link GenericApplicationContext} for the specified
	 * {@link MergedContextConfiguration}. Throw an {@link IllegalStateException} if
	 * the {@link MergedContextConfiguration} is not generated from a supported
	 * test context bootstrapper.
	 * @param config the config to handle
	 * @return the context
	 * @see #supports(TestContextBootstrapper)
	 */
	GenericApplicationContext prepareTestContext(MergedContextConfiguration config);

}
