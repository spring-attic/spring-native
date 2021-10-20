package org.springframework.aot.context.bootstrap.generator.infrastructure;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeReflectionEntry;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link CompositeBootstrapWriterContext}.
 *
 * @author Stephane Nicoll
 */
class CompositeBootstrapWriterContextTests {

	private final CompositeBootstrapWriterContext composite = new CompositeBootstrapWriterContext("com.example");

	@Test
	void createBootstrapWriterContextWithClassName() {
		BootstrapWriterContext context = this.composite.createBootstrapWriterContext("Test");
		assertThat(context).isNotNull();
		assertThat(context.getMainBootstrapClass().getClassName())
				.isEqualTo(ClassName.get("com.example", "Test"));
	}

	@Test
	void createBootstrapWriterContextWithClassNameThatIsAlreadyRegistered() {
		this.composite.createBootstrapWriterContext("Test");
		assertThatIllegalArgumentException().isThrownBy(() -> this.composite.createBootstrapWriterContext("Test"))
				.withMessageContaining("'Test'");
	}

	@Test
	void createBootstrapWriterContextWithClassNameApplyDefaultFactoryForMainBootstrapClass() {
		BootstrapWriterContext context = this.composite.createBootstrapWriterContext("Test");
		assertThat(context).isNotNull();
		JavaFile javaFile = context.getMainBootstrapClass().toJavaFile();
		assertThat(javaFile.typeSpec.modifiers).containsOnly(Modifier.PUBLIC);
		assertThat(javaFile.typeSpec.superinterfaces).containsOnly(
				ParameterizedTypeName.get(ApplicationContextInitializer.class, GenericApplicationContext.class));
	}

	@Test
	void createBootstrapWriterContextWithClassNameApplyDefaultFactoryForBootstrapClass() {
		BootstrapWriterContext context = this.composite.createBootstrapWriterContext("Test");
		assertThat(context).isNotNull();
		JavaFile javaFile = context.getBootstrapClass("com.example.another").toJavaFile();
		assertThat(javaFile.typeSpec.modifiers).containsOnly(Modifier.PUBLIC, Modifier.FINAL);
		assertThat(javaFile.typeSpec.superinterfaces).isEmpty();
	}

	@Test
	void createBootstrapWriterContextWithBootstrapClassFactoryForMainBootstrapClass() {
		Function<String, BootstrapClass> factory = mockBootstrapClassFactory();
		BootstrapClass mainBootstrapClass = BootstrapClass.of(ClassName.get("com.example", "Main"));
		given(factory.apply("com.example")).willReturn(mainBootstrapClass);
		BootstrapWriterContext context = this.composite.createBootstrapWriterContext("test", factory);
		assertThat(context).isNotNull();
		assertThat(context.getMainBootstrapClass()).isSameAs(mainBootstrapClass);
		verify(factory).apply("com.example");
	}

	@Test
	void createBootstrapWriterContextWithBootstrapClassFactoryForBootstrapClass() {
		Function<String, BootstrapClass> factory = mockBootstrapClassFactory();
		BootstrapClass bootstrapClass = BootstrapClass.of(ClassName.get("com.example.another", "Main"));
		given(factory.apply("com.example.another")).willReturn(bootstrapClass);
		BootstrapWriterContext context = this.composite.createBootstrapWriterContext("test", factory);
		assertThat(context).isNotNull();
		assertThat(context.getBootstrapClass("com.example.another")).isSameAs(bootstrapClass);
		verify(factory).apply("com.example.another");
	}

	@Test
	void compositeKeepsTrackOfJavaFiles() {
		BootstrapWriterContext test1 = composite.createBootstrapWriterContext("Test1");
		test1.getBootstrapClass("com.example.test1");
		BootstrapWriterContext test2 = composite.createBootstrapWriterContext("Test2");
		test2.getBootstrapClass("com.example.test2");
		List<JavaFile> javaFiles = composite.toJavaFiles();
		assertThat(javaFiles.stream().map((javaFile) -> javaFile.packageName + "." + javaFile.typeSpec.name)).containsOnly(
				"com.example.Test1", "com.example.test1.Test1", "com.example.Test2", "com.example.test2.Test2");
	}

	@Test
	void compositeKeepsTrackOfNativeConfiguration() {
		composite.createBootstrapWriterContext("Test1").getNativeConfigurationRegistry()
				.reflection().forType(String.class);
		composite.createBootstrapWriterContext("Test2").getNativeConfigurationRegistry()
				.reflection().forType(Integer.class);
		List<Class<?>> reflectionTypes = composite.getNativeConfigurationRegistry().reflection().getEntries().stream()
				.map(NativeReflectionEntry::getType).collect(Collectors.toList());
		assertThat(reflectionTypes).containsOnly(String.class, Integer.class);
	}

	@SuppressWarnings("unchecked")
	private Function<String, BootstrapClass> mockBootstrapClassFactory() {
		return mock(Function.class);
	}

}
