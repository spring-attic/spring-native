/*
 * Copyright 2019-2022 the original author or authors.
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

package org.springframework.cloud;

import com.squareup.javapoet.CodeBlock.Builder;

import org.springframework.aot.beans.factory.config.NoOpScope;
import org.springframework.aot.context.bootstrap.generator.infrastructure.BootstrapWriterContext;
import org.springframework.aot.support.BeanFactoryProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.MergedAnnotation;

/**
 * Register a no-op scope if {@code @RefreshScope} is used by the application.
 *
 * @author Stephane Nicoll
 */
public class SpringCloudRefreshScopeHandler {

	private final BeanFactoryProcessor beanFactoryProcessor;

	public SpringCloudRefreshScopeHandler(ConfigurableListableBeanFactory beanFactory) {
		this.beanFactoryProcessor = new BeanFactoryProcessor(beanFactory);
	}

	public void writeNoOpRefreshScope(BootstrapWriterContext writerContext, Builder code) {
		if (hasRefreshScope()) {
			code.addStatement("beanFactory.registerScope($S, new $T())", "refresh", NoOpScope.class);
		}
	}

	private boolean hasRefreshScope() {
		return this.beanFactoryProcessor.beansWithAnnotation(Scope.class).anyMatch((descriptor) -> {
			String scopeName = MergedAnnotation.from(descriptor.getAnnotation()).getString("value");
			return "refresh".equals(scopeName);
		});
	}

}
