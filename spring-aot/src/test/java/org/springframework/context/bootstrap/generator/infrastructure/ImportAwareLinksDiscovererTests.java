package org.springframework.context.bootstrap.generator.infrastructure;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.BuildTimeBeanDefinitionsRegistrar;
import org.springframework.context.bootstrap.generator.infrastructure.reflect.RuntimeReflectionRegistry;
import org.springframework.context.bootstrap.generator.sample.callback.ImportAwareConfiguration;
import org.springframework.context.bootstrap.generator.sample.callback.ImportConfiguration;
import org.springframework.context.bootstrap.generator.sample.callback.NestedImportConfiguration;
import org.springframework.context.origin.BeanDefinitionDescriptor;
import org.springframework.context.origin.BeanDefinitionDescriptor.Type;
import org.springframework.context.origin.BeanFactoryStructure;
import org.springframework.context.origin.BeanFactoryStructureAnalyzer;
import org.springframework.context.support.GenericApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.entry;

/**
 * Tests for {@link ImportAwareLinksDiscoverer}.
 *
 * @author Stephane Nicoll
 */
class ImportAwareLinksDiscovererTests {

	private final BuildTimeBeanDefinitionsRegistrar registrar = new BuildTimeBeanDefinitionsRegistrar();

	@Test
	void buildImportAwareLinksWithInnerClassLinkToActualClass() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(NestedImportConfiguration.class);
		Map<String, Class<?>> importAwareLinks = createImportAwareInfrastructureBuilder(context)
				.buildImportAwareLinks(new RuntimeReflectionRegistry());
		assertThat(importAwareLinks).containsOnly(entry(
				ImportAwareConfiguration.class.getName(), NestedImportConfiguration.Nested.class));
	}

	@Test
	void buildImportAwareLinksWithInnerClassRegisterClassResource() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(NestedImportConfiguration.class);
		RuntimeReflectionRegistry registry = new RuntimeReflectionRegistry();
		createImportAwareInfrastructureBuilder(context).buildImportAwareLinks(registry);
		assertThat(registry.getResourcesDescriptor().getPatterns()).singleElement().isEqualTo(
				"org/springframework/context/bootstrap/generator/sample/callback/NestedImportConfiguration\\$Nested.class");
	}

	@Test
	void buildImportAwareWithResolvableTypeUseIt() {
		ImportAwareLinksDiscoverer builder = createForImportingCandidate(BeanDefinitionBuilder
				.rootBeanDefinition(ImportConfiguration.class).getBeanDefinition());
		assertThat(builder.buildImportAwareLinks(new RuntimeReflectionRegistry())).containsOnly(entry(
				ImportAwareConfiguration.class.getName(), ImportConfiguration.class));
	}

	@Test
	void buildImportAwareLinksWithNoImportingBeanDefinitionIgnoreEntry() {
		RootBeanDefinition importingBeanDefinition = new RootBeanDefinition();
		importingBeanDefinition.setBeanClass(ImportConfiguration.class);
		ImportAwareLinksDiscoverer builder = createForImportingCandidate(null);
		assertThat(builder.buildImportAwareLinks(new RuntimeReflectionRegistry())).isEmpty();
	}

	@Test
	void buildImportAwareLinksWithNoResolvableTypeUseBeanClass() {
		RootBeanDefinition importingBeanDefinition = new RootBeanDefinition();
		importingBeanDefinition.setBeanClass(ImportConfiguration.class);
		ImportAwareLinksDiscoverer builder = createForImportingCandidate(importingBeanDefinition);
		assertThat(builder.buildImportAwareLinks(new RuntimeReflectionRegistry())).containsOnly(entry(
				ImportAwareConfiguration.class.getName(), ImportConfiguration.class));
	}

	@Test
	void buildImportAwareLinksWithNoResolvableTypeUseClassName() {
		RootBeanDefinition importingBeanDefinition = new RootBeanDefinition();
		importingBeanDefinition.setBeanClassName(ImportConfiguration.class.getName());
		ImportAwareLinksDiscoverer builder = createForImportingCandidate(importingBeanDefinition);
		assertThat(builder.buildImportAwareLinks(new RuntimeReflectionRegistry())).containsOnly(entry(
				ImportAwareConfiguration.class.getName(), ImportConfiguration.class));
	}

	@Test
	void buildImportAwareLinksWithNoTypeIsIgnored() {
		RootBeanDefinition importingBeanDefinition = new RootBeanDefinition();
		ImportAwareLinksDiscoverer builder = createForImportingCandidate(importingBeanDefinition);
		assertThat(builder.buildImportAwareLinks(new RuntimeReflectionRegistry())).isEmpty();
	}

	@Test
	void buildImportAwareLinksWithInvalidClassNameThrowsException() {
		RootBeanDefinition importingBeanDefinition = new RootBeanDefinition();
		importingBeanDefinition.setBeanClassName("does-not-exist");
		ImportAwareLinksDiscoverer builder = createForImportingCandidate(importingBeanDefinition);
		assertThatIllegalStateException().isThrownBy(() -> builder.buildImportAwareLinks(new RuntimeReflectionRegistry()))
				.withMessageContaining("Bean definition refers to invalid class");
	}

	private ImportAwareLinksDiscoverer createForImportingCandidate(BeanDefinition importingBeanDefinition) {
		Map<String, BeanDefinitionDescriptor> structure = new LinkedHashMap<>();
		if (importingBeanDefinition != null) {
			structure.put("importing", BeanDefinitionDescriptor.unresolved("importing", importingBeanDefinition)
					.resolve(Type.CONFIGURATION, Set.of()));
		}
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setBeanClass(ImportAwareConfiguration.class);
		structure.put("imported", BeanDefinitionDescriptor.unresolved("imported", beanDefinition)
				.resolve(Type.CONFIGURATION, Set.of("importing")));
		BeanFactoryStructure beanFactoryStructure = new BeanFactoryStructure(structure);
		return new ImportAwareLinksDiscoverer(beanFactoryStructure, getClass().getClassLoader());
	}

	private ImportAwareLinksDiscoverer createImportAwareInfrastructureBuilder(GenericApplicationContext context) {
		ConfigurableListableBeanFactory beanFactory = this.registrar.processBeanDefinitions(context);
		BeanFactoryStructure structure = new BeanFactoryStructureAnalyzer(context.getClassLoader()).analyze(beanFactory);
		return new ImportAwareLinksDiscoverer(structure, context.getClassLoader());
	}

}
