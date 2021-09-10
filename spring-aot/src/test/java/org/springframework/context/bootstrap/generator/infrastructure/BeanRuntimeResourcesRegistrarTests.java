package org.springframework.context.bootstrap.generator.infrastructure;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.infrastructure.reflect.RuntimeReflectionEntry;
import org.springframework.context.bootstrap.generator.infrastructure.reflect.RuntimeReflectionRegistry;
import org.springframework.context.bootstrap.generator.sample.callback.AsyncConfiguration;
import org.springframework.context.bootstrap.generator.sample.injection.InjectionComponent;
import org.springframework.context.bootstrap.generator.sample.injection.InjectionConfiguration;
import org.springframework.nativex.hint.Flag;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BeanRuntimeResourcesRegistrar}.
 *
 * @author Stephane Nicoll
 */
class BeanRuntimeResourcesRegistrarTests {

	private final BeanRuntimeResourcesRegistrar registrar = new BeanRuntimeResourcesRegistrar();

	@Test
	void registerReflectionEntriesForInstanceCreator() {
		Constructor<?> instanceCreator = InjectionComponent.class.getDeclaredConstructors()[0];
		RuntimeReflectionRegistry registry = register(BeanInstanceDescriptor.of(InjectionComponent.class)
				.withInstanceCreator(instanceCreator).build());
		assertThat(registry.getEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(InjectionComponent.class);
			assertThat(entry.getMethods()).containsOnly(instanceCreator);
			assertThat(entry.getFields()).isEmpty();
		});
	}

	@Test
	void registerReflectionEntriesForMethodInjectionPoint() {
		Constructor<?> instanceCreator = InjectionComponent.class.getDeclaredConstructors()[0];
		Method injectionPoint = ReflectionUtils.findMethod(InjectionComponent.class, "setCounter", Integer.class);
		RuntimeReflectionRegistry registry = register(BeanInstanceDescriptor.of(InjectionComponent.class)
				.withInstanceCreator(instanceCreator).withInjectionPoint(injectionPoint, false).build());
		assertThat(registry.getEntries()).anySatisfy((entry) -> {
			assertThat(entry.getType()).isEqualTo(InjectionComponent.class);
			assertThat(entry.getMethods()).containsOnly(instanceCreator, injectionPoint);
			assertThat(entry.getFields()).isEmpty();
		});
		assertThat(registry.getEntries()).anySatisfy((entry) -> {
			assertThat(entry.getType()).isEqualTo(Autowired.class);
			assertThat(entry.getFlags()).containsOnly(Flag.allDeclaredMethods);
		});
		assertThat(registry.getEntries()).hasSize(2);
	}

	@Test
	void registerReflectionEntriesForFieldInjectionPoint() {
		Constructor<?> instanceCreator = InjectionComponent.class.getDeclaredConstructors()[0];
		Field injectionPoint = ReflectionUtils.findField(InjectionComponent.class, "counter");
		RuntimeReflectionRegistry registry = register(BeanInstanceDescriptor.of(InjectionComponent.class)
				.withInstanceCreator(instanceCreator).withInjectionPoint(injectionPoint, false).build());
		assertThat(registry.getEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(InjectionComponent.class);
			assertThat(entry.getMethods()).containsOnly(instanceCreator);
			assertThat(entry.getFields()).containsOnly(injectionPoint);
		});
	}

	@Test
	void registerReflectionEntriesForProperties() {
		Constructor<?> instanceCreator = InjectionConfiguration.class.getDeclaredConstructors()[0];
		Method nameWriteMethod = ReflectionUtils.findMethod(InjectionConfiguration.class, "setName", String.class);
		Method counterWriteMethod = ReflectionUtils.findMethod(InjectionConfiguration.class, "setCounter", Integer.class);
		RuntimeReflectionRegistry registry = register(BeanInstanceDescriptor.of(InjectionComponent.class)
				.withInstanceCreator(instanceCreator).withProperty(nameWriteMethod, new PropertyValue("name", "Hello"))
				.withProperty(counterWriteMethod, new PropertyValue("counter", 42)).build());
		assertThat(registry.getEntries()).singleElement().satisfies((entry) -> {
			assertThat(entry.getType()).isEqualTo(InjectionConfiguration.class);
			assertThat(entry.getMethods()).containsOnly(instanceCreator, nameWriteMethod, counterWriteMethod);
			assertThat(entry.getFields()).isEmpty();
		});
	}

	@Test
	void registerReflectionEntriesForClassRuntimeAnnotations() {
		RuntimeReflectionRegistry registry = register(BeanInstanceDescriptor.of(AsyncConfiguration.class).build());
		assertThat(registry.getEntries()).singleElement().satisfies(annotation(EnableAsync.class));
	}

	private Consumer<RuntimeReflectionEntry> annotation(Class<? extends Annotation> annotationType) {
		return (entry) -> {
			assertThat(entry.getType()).isEqualTo(annotationType);
			assertThat(entry.getFlags()).containsOnly(Flag.allDeclaredMethods);
			assertThat(entry.getMethods()).isEmpty();
			assertThat(entry.getFields()).isEmpty();
		};
	}

	private RuntimeReflectionRegistry register(BeanInstanceDescriptor descriptor) {
		RuntimeReflectionRegistry registry = new RuntimeReflectionRegistry();
		this.registrar.register(registry, descriptor);
		return registry;
	}

}
