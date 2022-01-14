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

package org.springframework.aot.test.boot;

import java.util.Arrays;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;

import org.springframework.aot.context.bootstrap.generator.bean.support.MultiCodeBlock;
import org.springframework.aot.test.context.bootstrap.generator.AotTestContextProcessor;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.test.context.ReactiveWebMergedContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.SpringBootTestArgsAccessor;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContextBootstrapper;
import org.springframework.test.context.web.WebMergedContextConfiguration;

/**
 * An {@link AotTestContextProcessor} that handles Spring Boot test. This includes support
 * for {@link SpringBootTest} as well as slice tests.
 *
 * @author Stephane Nicoll
 */
@Order(0)
class SpringBootAotTestContextProcessor implements AotTestContextProcessor {

	private final SpringBootBuildTimeConfigContextLoader contextLoader = new SpringBootBuildTimeConfigContextLoader();

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

	@Override
	public CodeBlock writeInstanceSupplier(MergedContextConfiguration config, ClassName applicationContextInitializer) {
		String[] args = SpringBootTestArgsAccessor.get(config.getContextCustomizers());

		Builder code = CodeBlock.builder();
		code.add("() -> new $T($T.class", AotSpringBootConfigContextLoader.class, applicationContextInitializer);
		WebApplicationType webApplicationType = detectWebApplicationType(config);
		if (!webApplicationType.equals(WebApplicationType.NONE)) {
			code.add(", $T.$L, $T.$L", WebApplicationType.class, webApplicationType,
					WebEnvironment.class, detectWebEnvironment(config));
		}
		if (args.length > 0) {
			MultiCodeBlock multi = new MultiCodeBlock();
			Arrays.stream(args).forEach((arg) -> multi.add("$S", arg));
			code.add(", $L", multi.join(", "));
		}
		code.add(")");

		return code.build();
	}

	private WebApplicationType detectWebApplicationType(MergedContextConfiguration config) {
		if (config instanceof WebMergedContextConfiguration) {
			return WebApplicationType.SERVLET;
		}
		else if (config instanceof ReactiveWebMergedContextConfiguration) {
			return WebApplicationType.REACTIVE;
		}
		else {
			return WebApplicationType.NONE;
		}
	}

	private WebEnvironment detectWebEnvironment(MergedContextConfiguration config) {
		return MergedAnnotations.from(config.getTestClass(), SearchStrategy.TYPE_HIERARCHY).get(SpringBootTest.class)
				.getValue("webEnvironment", WebEnvironment.class).orElse(WebEnvironment.NONE);
	}

}
