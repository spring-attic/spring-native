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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * A {@link SpringApplication} used in the context of a test that uses an
 * {@link ApplicationContextInitializer} rather than parsing the context.
 *
 * @author Stephane Nicoll
 */
class SpringTestApplication extends SpringApplication {

	public SpringTestApplication(ClassLoader classLoader,
			Class<? extends ApplicationContextInitializer<?>> testContextInitializer) {
		setResourceLoader(new DefaultResourceLoader(classLoader));
		setInitializers(mergeInitializers(classLoader, testContextInitializer));
	}

	@Override
	protected void load(ApplicationContext context, Object[] sources) {
		// AOT mode does not require this.
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Collection<? extends ApplicationContextInitializer<?>> mergeInitializers(ClassLoader classLoader,
			Class<? extends ApplicationContextInitializer<?>> testContextInitializer) {
		List initializers = new ArrayList<>();
		initializers.add(BeanUtils.instantiateClass(testContextInitializer));
		initializers.addAll(SpringFactoriesLoader.loadFactories(ApplicationContextInitializer.class, classLoader));
		AnnotationAwareOrderComparator.sort(initializers);
		return initializers;
	}

}
