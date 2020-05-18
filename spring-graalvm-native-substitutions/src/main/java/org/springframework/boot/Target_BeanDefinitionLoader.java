package org.springframework.boot;

import java.util.HashSet;
import java.util.Set;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.Inject;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AbstractTypeHierarchyTraversingFilter;
import org.springframework.graalvm.substitutions.OnlyPresent;
import org.springframework.graalvm.substitutions.RemoveXmlSupport;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

@TargetClass(className = "org.springframework.boot.BeanDefinitionLoader", onlyWith = { OnlyPresent.class, RemoveXmlSupport.class })
final class Target_BeanDefinitionLoader {

	@Delete
	private XmlBeanDefinitionReader xmlReader;

	@Alias
	private Object[] sources;

	@Alias
	private AnnotatedBeanDefinitionReader annotatedReader;

	@Alias
	private ClassPathBeanDefinitionScanner scanner;

	@Alias
	private ResourceLoader resourceLoader;

	@Inject
	private BeanDefinitionRegistry registry;

	/**
	 * Create a new {@link BeanDefinitionLoader} that will load beans into the specified
	 * {@link BeanDefinitionRegistry}.
	 * @param registry the bean definition registry that will contain the loaded beans
	 * @param sources the bean sources
	 */
	@Substitute
	Target_BeanDefinitionLoader(BeanDefinitionRegistry registry, Object... sources) {
		Assert.notNull(registry, "Registry must not be null");
		Assert.notEmpty(sources, "Sources must not be empty");
		this.sources = sources;
		this.annotatedReader = new AnnotatedBeanDefinitionReader(registry);
		this.scanner = new ClassPathBeanDefinitionScanner(registry);
		this.scanner.addExcludeFilter(new ClassExcludeFilter(sources));
		this.registry = registry;
	}

	@Substitute
	void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		this.annotatedReader.setBeanNameGenerator(beanNameGenerator);
		this.scanner.setBeanNameGenerator(beanNameGenerator);
	}

	@Substitute
	void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
		this.scanner.setResourceLoader(resourceLoader);
	}

	@Substitute
	void setEnvironment(ConfigurableEnvironment environment) {
		this.annotatedReader.setEnvironment(environment);
		this.scanner.setEnvironment(environment);
	}

	/**
	 * Load the sources into the reader.
	 * @return the number of loaded beans
	 */
	@Substitute
	int load() {
		int count = 0;
		for (Object source : this.sources) {
			count += load(source);
		}
		return count;
	}

	@Substitute
	private int load(Object source) {
		Assert.notNull(source, "Source must not be null");
		if (source instanceof Class<?>) {
			return load((Class<?>) source);
		}
		if (source instanceof Resource) {
			return load((Resource) source);
		}
		if (source instanceof Package) {
			return load((Package) source);
		}
		if (source instanceof CharSequence) {
			return load((CharSequence) source);
		}
		throw new IllegalArgumentException("Invalid source type " + source.getClass());
	}

	@Substitute
	private int load(Class<?> source) {
		if (isComponent(source)) {
			this.annotatedReader.register(source);
			return 1;
		}
		return 0;
	}

	@Alias
	private boolean isComponent(Class<?> type) {
		return false;
	}

	@Substitute
	private int load(Package source) {
		return this.scanner.scan(source.getName());
	}

	@Substitute
	private int load(CharSequence source) {

		Environment env = (Environment)(registry instanceof EnvironmentCapable ? ((EnvironmentCapable)registry).getEnvironment() : new StandardEnvironment());
		String resolvedSource = env.resolvePlaceholders(source.toString());
		// Attempt as a Class
		try {
			return load(ClassUtils.forName(resolvedSource, null));
		}
		catch (IllegalArgumentException | ClassNotFoundException ex) {
			// swallow exception and continue
		}
		// Attempt as resources
		Resource[] resources = findResources(resolvedSource);
		int loadCount = 0;
		boolean atLeastOneResourceExists = false;
		for (Resource resource : resources) {
			if (isLoadCandidate(resource)) {
				atLeastOneResourceExists = true;
				loadCount += load(resource);
			}
		}
		if (atLeastOneResourceExists) {
			return loadCount;
		}
		// Attempt as package
		Package packageResource = findPackage(resolvedSource);
		if (packageResource != null) {
			return load(packageResource);
		}
		throw new IllegalArgumentException("Invalid source '" + resolvedSource + "'");
	}

	@Alias
	private boolean isLoadCandidate(Resource resource) {
		return false;
	}

	@Alias
	private Resource[] findResources(String source) {
		return null;
	}

	@Alias
	private Package findPackage(CharSequence source) {
		return null;
	}

	private static class ClassExcludeFilter extends AbstractTypeHierarchyTraversingFilter {

		private final Set<String> classNames = new HashSet<>();

		ClassExcludeFilter(Object... sources) {
			super(false, false);
			for (Object source : sources) {
				if (source instanceof Class<?>) {
					this.classNames.add(((Class<?>) source).getName());
				}
			}
		}

		@Override
		protected boolean matchClassName(String className) {
			return this.classNames.contains(className);
		}

	}

}