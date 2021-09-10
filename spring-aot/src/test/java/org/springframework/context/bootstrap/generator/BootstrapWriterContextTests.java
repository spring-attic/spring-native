package org.springframework.context.bootstrap.generator;

import org.junit.jupiter.api.Test;

import org.springframework.context.bootstrap.generator.infrastructure.BootstrapClass;
import org.springframework.context.bootstrap.generator.infrastructure.BootstrapWriterContext;

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
		assertThat(writerContext.getRuntimeReflectionRegistry()).isNotNull();
		assertThat(writerContext.getRuntimeReflectionRegistry().getEntries()).isEmpty();
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
