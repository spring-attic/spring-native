package org.springframework.context.bootstrap.generator.nativex;

import java.lang.reflect.Method;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.context.bootstrap.generator.infrastructure.nativex.NativeReflectionEntry;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FrameworkMethodsBeanNativeConfigurationProcessor}.
 *
 * @author Sébastien Deleuze
 */
public class FrameworkMethodsBeanNativeConfigurationProcessorTests {

	@Test
	void registerMethodsForMappingAnnotations() throws NoSuchMethodException {
		NativeConfigurationRegistry registry = register(BeanInstanceDescriptor.of(FooController.class).build());
		Method fooMethod = FooController.class.getDeclaredMethod("foo");
		Method barMethod = AbstractController.class.getDeclaredMethod("bar");
		assertThat(registry.reflection().getEntries()).hasSize(2).anySatisfy(method(fooMethod)).anySatisfy(method(barMethod));
	}

	private NativeConfigurationRegistry register(BeanInstanceDescriptor descriptor) {
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new FrameworkMethodsBeanNativeConfigurationProcessor().process(descriptor, registry);
		return registry;
	}

	private Consumer<NativeReflectionEntry> method(Method method) {
		return (entry) -> {
			assertThat(entry.getType()).isEqualTo(method.getDeclaringClass());
			assertThat(entry.getFlags()).isEmpty();
			assertThat(entry.getConstructors()).isEmpty();
			assertThat(entry.getMethods()).singleElement().isEqualTo(method);
			assertThat(entry.getFields()).isEmpty();
		};
	}

	@Controller
	class FooController extends AbstractController {

		@GetMapping
		String foo() {
			return "foo";
		}

		@EventListener
		void listener() {
		}

		@Bean
		void bean() {
		}
	}

	class AbstractController {

		@GetMapping
		String bar() {
			return "bar";
		}
	}
}
