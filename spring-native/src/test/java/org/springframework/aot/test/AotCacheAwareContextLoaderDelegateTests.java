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

package org.springframework.aot.test;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.SmartContextLoader;
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate;
import org.springframework.test.context.support.DefaultBootstrapContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Tests for {@link AotCacheAwareContextLoaderDelegate}.
 *
 * @author Stephane Nicoll
 * @author Sam Brannen
 */
class AotCacheAwareContextLoaderDelegateTests {

	private final ApplicationContext applicationContext = mock(ApplicationContext.class);

	private final SmartContextLoader contextLoader = mockSmartContextLoader(applicationContext);

	private final AotCacheAwareContextLoaderDelegate delegate = new AotCacheAwareContextLoaderDelegate();


	@AfterEach
	void resetAotContextLoaderUtils() {
		AotContextLoaderUtils.setContextLoaders(null);
	}

	@Test
	void loadContextWithMatchDelegatesToSmartContextLoader() throws Exception {
		AotContextLoaderUtils.setContextLoaders(Map.of(SampleTest.class.getName(), () -> contextLoader));
		assertThat(delegate.loadContextInternal(createMergedContextConfiguration(SampleTest.class)))
				.isSameAs(applicationContext);
		verify(contextLoader).loadContext(any(MergedContextConfiguration.class));
	}

	@Test
	void loadContextWithNoMatchUsesDefaultBehavior() throws Exception {
		AotContextLoaderUtils.setContextLoaders(Map.of(SampleTest.class.getName(), () -> contextLoader));
		ApplicationContext actual = delegate.loadContextInternal(
				createMergedContextConfiguration(SampleAnotherTest.class));
		assertThat(actual).isNotNull();
		verifyNoInteractions(contextLoader);
	}

	private SmartContextLoader mockSmartContextLoader(ApplicationContext applicationContext) {
		try {
			SmartContextLoader mock = mock(SmartContextLoader.class);
			given(mock.loadContext(any(MergedContextConfiguration.class))).willReturn(applicationContext);
			return mock;
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	private MergedContextConfiguration createMergedContextConfiguration(Class<?> testClass) {
		SpringBootTestContextBootstrapper bootstrapper = new SpringBootTestContextBootstrapper();
		bootstrapper.setBootstrapContext(new DefaultBootstrapContext(testClass,
				new DefaultCacheAwareContextLoaderDelegate()));
		return bootstrapper.buildMergedContextConfiguration();
	}


	@SpringBootTest(properties = "spring.main.web-application-type=none")
	static class SampleTest {
	}

	@SpringBootTest(properties = "spring.main.web-application-type=none")
	static class SampleAnotherTest {
	}

	@SpringBootConfiguration
	static class SampleConfiguration {
	}

}
