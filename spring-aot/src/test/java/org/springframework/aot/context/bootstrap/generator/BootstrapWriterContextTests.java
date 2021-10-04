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

package org.springframework.aot.context.bootstrap.generator;

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.infrastructure.BootstrapClass;
import org.springframework.aot.context.bootstrap.generator.infrastructure.BootstrapWriterContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BootstrapWriterContext}.
 *
 * @author Stephane Nicoll
 */
class BootstrapWriterContextTests {

	@Test
	void createDefaultLinkPackageName() {
		BootstrapClass defaultBootstrapClass = BootstrapClass.of("com.acme");
		BootstrapWriterContext writerContext = new BootstrapWriterContext(defaultBootstrapClass);
		assertThat(writerContext.getPackageName()).isEqualTo("com.acme");
		assertThat(writerContext.getMainBootstrapClass()).isSameAs(defaultBootstrapClass);
		assertThat(writerContext.getBootstrapClass("com.acme")).isSameAs(defaultBootstrapClass);
	}

	@Test
	void getBootstrapClassRegisterInstance() {
		BootstrapClass defaultBootstrapClass = BootstrapClass.of("com.acme");
		BootstrapWriterContext writerContext = new BootstrapWriterContext(defaultBootstrapClass);
		assertThat(writerContext.hasBootstrapClass("com.example")).isFalse();
		assertThat(writerContext.getBootstrapClass("com.example")).isNotNull();
		assertThat(writerContext.hasBootstrapClass("com.example")).isTrue();
	}

	@Test
	void getBootstrapClassReuseInstance() {
		BootstrapClass defaultBootstrapClass = BootstrapClass.of("com.acme");
		BootstrapWriterContext writerContext = new BootstrapWriterContext(defaultBootstrapClass);
		BootstrapClass bootstrapClass = writerContext.getBootstrapClass("com.example");
		assertThat(bootstrapClass.getClassName().packageName()).isEqualTo("com.example");
		assertThat(writerContext.getBootstrapClass("com.example")).isSameAs(bootstrapClass);
	}

	@Test
	void getRuntimeReflectionRegistry() {
		BootstrapWriterContext writerContext = new BootstrapWriterContext(BootstrapClass.of("com.acme"));
		assertThat(writerContext.getNativeConfigurationRegistry()).isNotNull();
		assertThat(writerContext.getNativeConfigurationRegistry().reflection().getEntries()).isEmpty();
	}

	@Test
	void toJavaFilesWithDefaultClass() {
		BootstrapClass defaultBootstrapClass = BootstrapClass.of("com.acme");
		BootstrapWriterContext writerContext = new BootstrapWriterContext(defaultBootstrapClass);
		assertThat(writerContext.toJavaFiles()).hasSize(1);
	}

	@Test
	void toJavaFilesWithDefaultClassAndAdditionalClasses() {
		BootstrapClass defaultBootstrapClass = BootstrapClass.of("com.acme");
		BootstrapWriterContext writerContext = new BootstrapWriterContext(defaultBootstrapClass);
		writerContext.getBootstrapClass("com.example");
		writerContext.getBootstrapClass("com.another");
		assertThat(writerContext.toJavaFiles()).hasSize(3);
	}

}
