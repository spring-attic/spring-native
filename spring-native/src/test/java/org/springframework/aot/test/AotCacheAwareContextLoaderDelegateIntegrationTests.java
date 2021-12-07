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
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.SmartContextLoader;
import org.springframework.test.context.cache.ContextCache;
import org.springframework.test.context.cache.DefaultContextCache;
import org.springframework.test.context.support.DelegatingSmartContextLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link AotCacheAwareContextLoaderDelegate} that interact with the
 * {@link DefaultContextCache}.
 *
 * @author Sam Brannen
 */
class AotCacheAwareContextLoaderDelegateIntegrationTests {

	private final ContextCache contextCache = new DefaultContextCache();

	private final ApplicationContext applicationContext = mock(ApplicationContext.class);

	private final SmartContextLoader aotSmartContextLoader = mockSmartContextLoader(this.applicationContext);

	private final AotCacheAwareContextLoaderDelegate delegate = createDelegate(this.contextCache, this.aotSmartContextLoader);

	@Test
	void nonAotTestCase() {
		MergedContextConfiguration mergedConfig = createMergedContextConfiguration(DemoTestCase.class);

		assertThat(contextCache.size()).isEqualTo(0);
		assertThat(delegate.isContextLoaded(mergedConfig)).isFalse();

		ApplicationContext context = delegate.loadContext(mergedConfig);
		assertThat(delegate.isContextLoaded(mergedConfig)).isTrue();
		assertThat(context).isNotNull();
		assertThat(contextCache.size()).isEqualTo(1);
		assertThat(contextCache.getHitCount()).isEqualTo(0);
		assertThat(contextCache.contains(mergedConfig)).isTrue();

		// Load again
		context = delegate.loadContext(mergedConfig);
		assertThat(delegate.isContextLoaded(mergedConfig)).isTrue();
		assertThat(context).isNotNull();
		assertThat(contextCache.size()).isEqualTo(1);
		assertThat(contextCache.getHitCount()).isEqualTo(1);
		assertThat(contextCache.contains(mergedConfig)).isTrue();

		// Load again
		context = delegate.loadContext(mergedConfig);
		assertThat(delegate.isContextLoaded(mergedConfig)).isTrue();
		assertThat(context).isNotNull();
		assertThat(contextCache.size()).isEqualTo(1);
		assertThat(contextCache.getHitCount()).isEqualTo(2);
		assertThat(contextCache.contains(mergedConfig)).isTrue();

		// Remove
		delegate.closeContext(mergedConfig, HierarchyMode.EXHAUSTIVE);
		assertThat(delegate.isContextLoaded(mergedConfig)).isFalse();
		assertThat(contextCache.size()).isEqualTo(0);
		assertThat(contextCache.contains(mergedConfig)).isFalse();
	}

	@Test
	void aotTestCase() {
		MergedContextConfiguration mergedConfig = createMergedContextConfiguration(AotTestCase.class);
		AotMergedContextConfiguration aotMergedConfig = new AotMergedContextConfiguration(AotTestCase.class,
				DemoApplicationContextInitializer.class, mergedConfig, delegate, null);

		assertThat(contextCache.size()).isEqualTo(0);
		assertThat(delegate.isContextLoaded(mergedConfig)).isFalse();

		ApplicationContext context = delegate.loadContext(mergedConfig);
		assertThat(delegate.isContextLoaded(mergedConfig)).isTrue();
		assertThat(context).isSameAs(applicationContext);
		assertThat(contextCache.size()).isEqualTo(1);
		assertThat(contextCache.getHitCount()).isEqualTo(0);
		// The ContextCache must not contain the original MergedContextConfiguration.
		assertThat(contextCache.contains(mergedConfig)).isFalse();
		// Instead, it must contain the AotMergedContextConfiguration.
		assertThat(contextCache.contains(aotMergedConfig)).isTrue();

		// Load again
		context = delegate.loadContext(mergedConfig);
		assertThat(delegate.isContextLoaded(mergedConfig)).isTrue();
		assertThat(context).isSameAs(applicationContext);
		assertThat(contextCache.size()).isEqualTo(1);
		assertThat(contextCache.getHitCount()).isEqualTo(1);
		// The ContextCache must not contain the original MergedContextConfiguration.
		assertThat(contextCache.contains(mergedConfig)).isFalse();
		// Instead, it must contain the AotMergedContextConfiguration.
		assertThat(contextCache.contains(aotMergedConfig)).isTrue();

		// Load again
		context = delegate.loadContext(mergedConfig);
		assertThat(delegate.isContextLoaded(mergedConfig)).isTrue();
		assertThat(context).isSameAs(applicationContext);
		assertThat(contextCache.size()).isEqualTo(1);
		assertThat(contextCache.getHitCount()).isEqualTo(2);
		// The ContextCache must not contain the original MergedContextConfiguration.
		assertThat(contextCache.contains(mergedConfig)).isFalse();
		// Instead, it must contain the AotMergedContextConfiguration.
		assertThat(contextCache.contains(aotMergedConfig)).isTrue();

		// Remove
		delegate.closeContext(mergedConfig, HierarchyMode.EXHAUSTIVE);
		assertThat(delegate.isContextLoaded(mergedConfig)).isFalse();
		assertThat(contextCache.size()).isEqualTo(0);
		assertThat(contextCache.contains(mergedConfig)).isFalse();
		assertThat(contextCache.contains(aotMergedConfig)).isFalse();
	}

	private MergedContextConfiguration createMergedContextConfiguration(Class<?> testClass) {
		return new MergedContextConfiguration(testClass, null, new Class<?>[] { DemoConfiguration.class },
				Set.of(DemoApplicationContextInitializer.class), null, new DelegatingSmartContextLoader());
	}

	private static AotCacheAwareContextLoaderDelegate createDelegate(ContextCache contextCache, SmartContextLoader aotSmartContextLoader) {
		AotTestMappings aotTestMappings = new AotTestMappings(
				Map.of(AotTestCase.class.getName(), () -> aotSmartContextLoader),
				Map.of(AotTestCase.class.getName(), DemoApplicationContextInitializer.class));
		return new AotCacheAwareContextLoaderDelegate(aotTestMappings, contextCache);
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


	static class AotTestCase {
	}

	static class DemoTestCase {
	}

	@Configuration
	static class DemoConfiguration {
	}

	static class DemoApplicationContextInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
		@Override
		public void initialize(GenericApplicationContext applicationContext) {
		}
	}

}
