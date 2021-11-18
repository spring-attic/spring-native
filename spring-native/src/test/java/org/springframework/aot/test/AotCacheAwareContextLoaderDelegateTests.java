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
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.SmartContextLoader;
import org.springframework.test.context.cache.ContextCache;
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate;
import org.springframework.test.context.support.DefaultBootstrapContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Tests for {@link AotCacheAwareContextLoaderDelegate}.
 *
 * @author Stephane Nicoll
 * @author Sam Brannen
 */
class AotCacheAwareContextLoaderDelegateTests {

	private final ContextCache contextCache = mock(ContextCache.class);

	private final ApplicationContext applicationContext = mock(ApplicationContext.class);

	private final SmartContextLoader aotSmartContextLoader = mockSmartContextLoader(this.applicationContext);


	@Test
	void loadContextWithMatchUsesAotInfrastructure() throws Exception {
		AotCacheAwareContextLoaderDelegate delegate = createDemoDelegate();

		MergedContextConfiguration mergedConfig = createMergedContextConfiguration(SampleTest.class);
		assertThat(delegate.loadContext(mergedConfig)).isSameAs(this.applicationContext);

		// loadContext() must actually be invoked with the original MergedContextConfiguration
		// instead of the AotMergedContextConfiguration, so that the ContextLoader can process
		// active profiles, test property sources, etc. that are picked up at run time.
		verify(this.aotSmartContextLoader, times(0)).loadContext(any(AotMergedContextConfiguration.class));
		ArgumentCaptor<MergedContextConfiguration> mergedConfigCaptor = ArgumentCaptor.forClass(MergedContextConfiguration.class);
		verify(this.aotSmartContextLoader).loadContext(mergedConfigCaptor.capture());
		assertThat(mergedConfigCaptor.getValue()).isSameAs(mergedConfig);

		// On the flip side, the AotMergedContextConfiguration should be used instead of the
		// original MergedContextConfiguration for the context cache.
		verifyCached(AotMergedContextConfiguration.class);
		verifyNotCached(MergedContextConfiguration.class);
	}

	@Test
	void loadContextWithoutMatchUsesDefaultBehavior() throws Exception {
		AotCacheAwareContextLoaderDelegate delegate = createDemoDelegate();

		ApplicationContext actual = delegate.loadContext(createMergedContextConfiguration(SampleAnotherTest.class));
		assertThat(actual).isNotNull();
		assertThat(actual.getEnvironment().getProperty("spring.main.web-application-type")).isEqualTo("none");

		verifyNoInteractions(this.aotSmartContextLoader);
		verifyCached(MergedContextConfiguration.class);
		verifyNotCached(AotMergedContextConfiguration.class);
	}

	@Test
	void isContextLoadedWithMatchUsesAotInfrastructure() throws Exception {
		AotCacheAwareContextLoaderDelegate delegate = createDemoDelegate();

		delegate.isContextLoaded(createMergedContextConfiguration(SampleTest.class));

		verify(this.contextCache, times(0)).contains(exactInstanceOf(MergedContextConfiguration.class));
		verify(this.contextCache).contains(any(AotMergedContextConfiguration.class));
	}

	@Test
	void isContextLoadedWithoutMatchUsesDefaultBehavior() throws Exception {
		AotCacheAwareContextLoaderDelegate delegate = createDemoDelegate();

		delegate.isContextLoaded(createMergedContextConfiguration(SampleAnotherTest.class));

		verify(this.contextCache, times(0)).contains(any(AotMergedContextConfiguration.class));
		verify(this.contextCache).contains(exactInstanceOf(MergedContextConfiguration.class));
	}

	@Test
	void closeContextWithMatchUsesAotInfrastructure() throws Exception {
		AotCacheAwareContextLoaderDelegate delegate = createDemoDelegate();

		delegate.closeContext(createMergedContextConfiguration(SampleTest.class), HierarchyMode.CURRENT_LEVEL);

		verify(this.contextCache, times(0)).remove(exactInstanceOf(MergedContextConfiguration.class), any(HierarchyMode.class));
		verify(this.contextCache).remove(any(AotMergedContextConfiguration.class), any(HierarchyMode.class));
	}

	@Test
	void closeContextWithoutMatchUsesDefaultBehavior() throws Exception {
		AotCacheAwareContextLoaderDelegate delegate = createDemoDelegate();

		delegate.closeContext(createMergedContextConfiguration(SampleAnotherTest.class), HierarchyMode.CURRENT_LEVEL);

		verify(this.contextCache, times(0)).remove(any(AotMergedContextConfiguration.class), any(HierarchyMode.class));
		verify(this.contextCache).remove(exactInstanceOf(MergedContextConfiguration.class), any(HierarchyMode.class));
	}


	private AotCacheAwareContextLoaderDelegate createDemoDelegate() {
		return createAotCacheAwareContextLoaderDelegate(
				Map.of(SampleTest.class.getName(), () -> this.aotSmartContextLoader),
				Map.of(SampleTest.class.getName(), DemoApplicationContextInitializer.class));
	}

	private AotCacheAwareContextLoaderDelegate createAotCacheAwareContextLoaderDelegate(
			Map<String, Supplier<SmartContextLoader>> contextLoaders,
			Map<String, Class<? extends ApplicationContextInitializer<?>>> contextInitializers) {
		AotContextLoader aotContextLoader = new AotContextLoader(contextLoaders, contextInitializers);
		return new AotCacheAwareContextLoaderDelegate(aotContextLoader, this.contextCache);
	}

	private void verifyCached(Class<? extends MergedContextConfiguration> type) {
		verify(this.contextCache).put(exactInstanceOf(type), any(ApplicationContext.class));
	}

	private void verifyNotCached(Class<? extends MergedContextConfiguration> type) {
		verify(this.contextCache, times(0)).put(exactInstanceOf(type), any(ApplicationContext.class));
	}

	private static SmartContextLoader mockSmartContextLoader(ApplicationContext applicationContext) {
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
				new DefaultCacheAwareContextLoaderDelegate(this.contextCache)));
		return bootstrapper.buildMergedContextConfiguration();
	}

	/**
	 * Performs an exact type check instead of an "instance of" check.
	 */
	private static <T> T exactInstanceOf(Class<T> type) {
		return argThat(arg -> arg.getClass() == type);
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

	static class DemoApplicationContextInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
		@Override
		public void initialize(GenericApplicationContext applicationContext) {
		}
	}

}
