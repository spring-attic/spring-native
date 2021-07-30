package org.springframework.context.bootstrap.generator;

import com.squareup.javapoet.ClassName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BootstrapWriterContext}.
 *
 * @author Stephane Nicoll
 */
class BootstrapWriterContextTests {

	@Test
	void createDefaultLinkPackageName() {
		BootstrapClass defaultBootstrapClass = new BootstrapClass("com.acme", "Test");
		BootstrapWriterContext writerContext = new BootstrapWriterContext(defaultBootstrapClass);
		assertThat(writerContext.getPackageName()).isEqualTo("com.acme");
		assertThat(writerContext.getBootstrapClass("com.acme")).isSameAs(defaultBootstrapClass);
	}

	@Test
	void getBootstrapClassReuseInstance() {
		BootstrapClass defaultBootstrapClass = new BootstrapClass("com.acme", "Test");
		BootstrapWriterContext writerContext = new BootstrapWriterContext(defaultBootstrapClass);
		BootstrapClass bootstrapClass = writerContext.getBootstrapClass("com.example");
		assertThat(bootstrapClass.getClassName()).isEqualTo(ClassName.get("com.example",
				BootstrapWriterContext.BOOTSTRAP_CLASS_NAME));
		assertThat(writerContext.getBootstrapClass("com.example")).isSameAs(bootstrapClass);
	}

	@Test
	void toJavaFilesWithDefaultClass() {
		BootstrapClass defaultBootstrapClass = new BootstrapClass("com.acme", "Test");
		BootstrapWriterContext writerContext = new BootstrapWriterContext(defaultBootstrapClass);
		assertThat(writerContext.toJavaFiles()).hasSize(1);
	}

	@Test
	void toJavaFilesWithDefaultClassAndAdditionalClasses() {
		BootstrapClass defaultBootstrapClass = new BootstrapClass("com.acme", "Test");
		BootstrapWriterContext writerContext = new BootstrapWriterContext(defaultBootstrapClass);
		writerContext.getBootstrapClass("com.example");
		writerContext.getBootstrapClass("com.another");
		assertThat(writerContext.toJavaFiles()).hasSize(3);
	}

}
