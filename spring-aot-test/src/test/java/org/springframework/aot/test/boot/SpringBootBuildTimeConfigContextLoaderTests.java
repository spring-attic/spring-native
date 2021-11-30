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

import org.junit.jupiter.api.Test;

import org.springframework.aot.test.samples.app.SampleApplicationTests;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate;
import org.springframework.test.context.support.DefaultBootstrapContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SpringBootBuildTimeConfigContextLoader}.
 *
 * @author Stephane Nicoll
 */
class SpringBootBuildTimeConfigContextLoaderTests {

	private final SpringBootBuildTimeConfigContextLoader contextLoader = new SpringBootBuildTimeConfigContextLoader();

	@Test
	void loadContextForSpringBootTest() {
		GenericApplicationContext context = loadContext(SampleApplicationTests.class);
		assertThat(context.isRunning()).isFalse();
	}

	private GenericApplicationContext loadContext(Class<?> testClass) {
		MergedContextConfiguration contextConfiguration = createMergedContextConfiguration(testClass);
		try {
			ApplicationContext context = this.contextLoader.loadContext(contextConfiguration);
			assertThat(context).isInstanceOf(GenericApplicationContext.class);
			return (GenericApplicationContext) context;
		}
		catch (Exception ex) {
			throw new IllegalStateException("Failed to process " + testClass, ex);
		}
	}

	private MergedContextConfiguration createMergedContextConfiguration(Class<?> testClass) {
		SpringBootTestContextBootstrapper bootstrapper = new SpringBootTestContextBootstrapper();
		bootstrapper.setBootstrapContext(new DefaultBootstrapContext(testClass, new DefaultCacheAwareContextLoaderDelegate()));
		return bootstrapper.buildMergedContextConfiguration();
	}

}
