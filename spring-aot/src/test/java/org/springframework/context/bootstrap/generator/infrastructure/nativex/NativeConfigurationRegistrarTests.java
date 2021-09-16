package org.springframework.context.bootstrap.generator.infrastructure.nativex;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link NativeConfigurationRegistrar}.
 *
 * @author Stephane Nicoll
 */
class NativeConfigurationRegistrarTests {

	@Test
	void processBeanFactoryInvokeProcessorsInOrder() {
		BeanFactoryNativeConfigurationProcessor first = mock(BeanFactoryNativeConfigurationProcessor.class);
		BeanFactoryNativeConfigurationProcessor second = mock(BeanFactoryNativeConfigurationProcessor.class);
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new NativeConfigurationRegistrar(beanFactory, List.of(first, second)).processBeanFactory(registry);
		InOrder ordered = inOrder(first, second);
		ordered.verify(first).process(beanFactory, registry);
		ordered.verify(second).process(beanFactory, registry);
	}

	@Test
	void processBeanFactoryLoadFromSpringFactories() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.setBeanClassLoader(new CustomSpringFactoriesClassLoader("bean-factory-processors.factories"));
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new NativeConfigurationRegistrar(beanFactory).processBeanFactory(registry);
		assertThat(registry.reflection().getEntries()).singleElement().satisfies((entry) ->
				assertThat(entry.getType()).isEqualTo(NativeConfigurationRegistrarTests.class));
	}

	@Test
	void processBeansLoadsFromSpringFactories() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.setBeanClassLoader(new CustomSpringFactoriesClassLoader("bean-processors.factories"));
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new NativeConfigurationRegistrar(beanFactory).processBeans(registry,
				List.of(BeanInstanceDescriptor.of(String.class).build(),
						BeanInstanceDescriptor.of(Integer.class).build()));
		List<Class<?>> collect = registry.reflection().getEntries().stream()
				.map(NativeReflectionEntry::getType).collect(Collectors.toList());
		assertThat(collect).containsOnly(String.class, Integer.class);
	}

	@Test
	void processBeansInvokeBeanFactoryAware() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("testBean", BeanDefinitionBuilder.rootBeanDefinition(
				NativeConfigurationRegistrarTests.class).getBeanDefinition());
		beanFactory.setBeanClassLoader(new CustomSpringFactoriesClassLoader("bean-processors-bean-factory-aware.factories"));
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new NativeConfigurationRegistrar(beanFactory).processBeans(registry,
				List.of(BeanInstanceDescriptor.of(String.class).build()));
		assertThat(registry.resources().toResourcesDescriptor().getPatterns()).singleElement().satisfies((pattern) ->
				assertThat(pattern).endsWith("NativeConfigurationRegistrarTests.class"));
	}


	static class TestBeanFactoryNativeConfigurationProcessor implements BeanFactoryNativeConfigurationProcessor {

		@Override
		public void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
			registry.reflection().forType(NativeConfigurationRegistrarTests.class);
		}
	}

	static class TestBeanNativeConfigurationProcessor implements BeanNativeConfigurationProcessor {

		@Override
		public void process(BeanInstanceDescriptor descriptor, NativeConfigurationRegistry registry) {
			registry.reflection().forType(descriptor.getUserBeanClass());

		}
	}

	static class BeanFactoryAwareBeanNativeConfigurationProcessor implements BeanNativeConfigurationProcessor, BeanFactoryAware {

		private BeanFactory beanFactory;

		@Override
		public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
			this.beanFactory = beanFactory;
		}

		@Override
		public void process(BeanInstanceDescriptor descriptor, NativeConfigurationRegistry registry) {
			registry.resources().add(NativeResourcesEntry.ofClass(this.beanFactory.getType("testBean")));
		}

	}

	static class CustomSpringFactoriesClassLoader extends ClassLoader {

		private final String factoriesName;

		CustomSpringFactoriesClassLoader(String factoriesName) {
			super(NativeConfigurationRegistrarTests.class.getClassLoader());
			this.factoriesName = factoriesName;
		}

		@Override
		public Enumeration<URL> getResources(String name) throws IOException {
			if ("META-INF/spring.factories".equals(name)) {
				return super.getResources("native-configuration-tests/" + this.factoriesName);
			}
			return super.getResources(name);
		}

	}

}
