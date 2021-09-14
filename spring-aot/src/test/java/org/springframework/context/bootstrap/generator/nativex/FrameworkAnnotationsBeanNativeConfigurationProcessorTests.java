package org.springframework.context.bootstrap.generator.nativex;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeReflectionEntry;
import org.springframework.context.bootstrap.generator.nativex.FrameworkAnnotationsBeanNativeConfigurationProcessor;
import org.springframework.context.bootstrap.generator.sample.callback.AsyncConfiguration;
import org.springframework.context.bootstrap.generator.sample.injection.InjectionComponent;
import org.springframework.nativex.hint.Flag;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FrameworkAnnotationsBeanNativeConfigurationProcessor}.
 *
 * @author Stephane Nicoll
 */
class FrameworkAnnotationsBeanNativeConfigurationProcessorTests {

	@Test
	void registerAnnotationsForClassAnnotations() {
		NativeConfigurationRegistry registry = register(BeanInstanceDescriptor.of(AsyncConfiguration.class).build());
		assertThat(registry.reflection().getEntries()).singleElement().satisfies(annotation(EnableAsync.class));
	}

	@Test
	void registerAnnotationsForMethodInjectionPoint() {
		Constructor<?> instanceCreator = InjectionComponent.class.getDeclaredConstructors()[0];
		Method injectionPoint = ReflectionUtils.findMethod(InjectionComponent.class, "setCounter", Integer.class);
		NativeConfigurationRegistry registry = register(BeanInstanceDescriptor.of(InjectionComponent.class)
				.withInstanceCreator(instanceCreator).withInjectionPoint(injectionPoint, false).build());
		assertThat(registry.reflection().getEntries()).singleElement().satisfies(annotation(Autowired.class));
	}

	private Consumer<NativeReflectionEntry> annotation(Class<? extends Annotation> annotationType) {
		return (entry) -> {
			assertThat(entry.getType()).isEqualTo(annotationType);
			assertThat(entry.getFlags()).containsOnly(Flag.allDeclaredMethods);
			assertThat(entry.getConstructors()).isEmpty();
			assertThat(entry.getMethods()).isEmpty();
			assertThat(entry.getFields()).isEmpty();
		};
	}

	private NativeConfigurationRegistry register(BeanInstanceDescriptor descriptor) {
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new FrameworkAnnotationsBeanNativeConfigurationProcessor().process(descriptor, registry);
		return registry;
	}

}
