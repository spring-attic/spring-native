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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.cache.ContextCache;
import org.springframework.test.context.support.DelegatingSmartContextLoader;

/**
 * Tests for {@link AotMergedContextConfiguration}.
 *
 * @author Sam Brannen
 */
class AotMergedContextConfigurationTests {

	private final AotCacheAwareContextLoaderDelegate delegate = new AotCacheAwareContextLoaderDelegate(
		new AotTestMappings(Map.of(), Map.of()), mock(ContextCache.class));

	private final MergedContextConfiguration mergedConfig = new MergedContextConfiguration(getClass(), null, null,
		Set.of(DemoApplicationContextInitializer.class), null, new DelegatingSmartContextLoader());

	private final AotMergedContextConfiguration aotMergedConfig1 = new AotMergedContextConfiguration(getClass(),
		DemoApplicationContextInitializer.class, mergedConfig, delegate, null);

	private final AotMergedContextConfiguration aotMergedConfig2 = new AotMergedContextConfiguration(getClass(),
		DemoApplicationContextInitializer.class, mergedConfig, delegate, null);

	private final AotMergedContextConfiguration aotMergedConfig3 = new AotMergedContextConfiguration(getClass(),
		DemoApplicationContextInitializer.class, mergedConfig, delegate, aotMergedConfig1);

	@Test
	void testEquals()  {
		assertThat(aotMergedConfig1).isEqualTo(aotMergedConfig1);
		assertThat(aotMergedConfig1).isEqualTo(aotMergedConfig2);

		assertThat(mergedConfig).isNotEqualTo(aotMergedConfig1);
		assertThat(aotMergedConfig1).isNotEqualTo(mergedConfig);
		assertThat(aotMergedConfig1).isNotEqualTo(aotMergedConfig3);
	}

	@Test
	void testHashCode() {
		assertThat(aotMergedConfig1).hasSameHashCodeAs(aotMergedConfig2);

		assertThat(aotMergedConfig1).doesNotHaveSameHashCodeAs(mergedConfig);
		assertThat(aotMergedConfig1).doesNotHaveSameHashCodeAs(aotMergedConfig3);
	}


	static class DemoApplicationContextInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
		@Override
		public void initialize(GenericApplicationContext applicationContext) {
		}
	}

}
