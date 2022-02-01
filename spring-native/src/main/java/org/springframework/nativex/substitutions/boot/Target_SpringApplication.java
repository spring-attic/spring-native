package org.springframework.nativex.substitutions.boot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.apache.commons.logging.Log;

import org.springframework.aot.SpringApplicationAotUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.BootstrapRegistryInitializer;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.nativex.AotModeDetector;
import org.springframework.nativex.substitutions.OnlyIfPresent;
import org.springframework.nativex.substitutions.WithAot;
import org.springframework.util.StringUtils;

/**
 * Why this substitution exists?
 *  - It provide different codepaths when AOT mode is enabled.
 *
 * How this substitution workarounds the problem?
 * - It skips BeanDefinitionLoader#load() when AOT mode is enabled in order to skip A LOT of runtime infrastructure to be included in the native image
 *   like AnnotatedBeanDefinitionReader, XmlBeanDefinitionReader, GroovyBeanDefinitionReader and ClassPathBeanDefinitionScanner.
 * - It uses org.springframework.aot.ContextBootstrapInitializer (via getSpringFactoriesInstances(ApplicationContextInitializer.class)) when AOT mode
 *   is enabled to load the precomputed beans.
 */
@TargetClass(className = "org.springframework.boot.SpringApplication", onlyWith = { WithAot.class, OnlyIfPresent.class })
final class Target_SpringApplication {

	@Alias
	private static Log logger;

	@Alias
	private ResourceLoader resourceLoader;

	@Alias
	private Set<Class<?>> primarySources;

	@Alias
	private WebApplicationType webApplicationType;

	@Alias
	private List<BootstrapRegistryInitializer> bootstrapRegistryInitializers;

	@Alias
	private Class<?> mainApplicationClass;

	@Alias
	private BeanNameGenerator beanNameGenerator;

	@Alias
	private ConfigurableEnvironment environment;


	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)
	private Set<String> sources = new LinkedHashSet<>();

	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)
	private Banner.Mode bannerMode = Banner.Mode.CONSOLE;

	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)
	private boolean logStartupInfo = true;

	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)
	private boolean addCommandLineProperties = true;

	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)
	private boolean addConversionService = true;

	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)
	private boolean headless = true;

	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)
	private boolean registerShutdownHook = true;

	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)
	private Set<String> additionalProfiles = Collections.emptySet();

	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)
	private ApplicationContextFactory applicationContextFactory = ApplicationContextFactory.DEFAULT;

	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)
	private ApplicationStartup applicationStartup = ApplicationStartup.DEFAULT;


	@Substitute
	public Target_SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
		this.resourceLoader = resourceLoader;
		this.primarySources = AotModeDetector.isAotModeEnabled() ?
				new LinkedHashSet<>(Arrays.asList(Object.class)) : new LinkedHashSet<>(Arrays.asList(primarySources));
		this.webApplicationType = Target_WebApplicationType.deduceFromClasspath();
		this.bootstrapRegistryInitializers = (List<BootstrapRegistryInitializer>) getSpringFactoriesInstances(BootstrapRegistryInitializer.class);
		setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
		this.mainApplicationClass = deduceMainApplicationClass();

		if (AotModeDetector.isRunningAotTests()) {
			setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
		}
		else if (AotModeDetector.isAotModeEnabled()) {
			setApplicationContextFactory(SpringApplicationAotUtils.AOT_FACTORY);
			List<ApplicationContextInitializer<?>> initializers = new ArrayList<>();
			initializers.add(SpringApplicationAotUtils.getBootstrapInitializer());
			initializers.add(new ConditionEvaluationReportLoggingListener());
			initializers.addAll((Collection)getSpringFactoriesInstances(ApplicationContextInitializer.class));
			setInitializers(initializers);
		}
		else if (!AotModeDetector.isGeneratingAotTests()) {
			setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
		}
	}

	@Substitute
	protected void load(ApplicationContext context, Object[] sources) {
		if (!AotModeDetector.isAotModeEnabled()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Loading source " + StringUtils.arrayToCommaDelimitedString(sources));
			}
			Target_BeanDefinitionLoader loader = createBeanDefinitionLoader(getBeanDefinitionRegistry(context), sources);
			if (this.beanNameGenerator != null) {
				loader.setBeanNameGenerator(this.beanNameGenerator);
			}
			if (this.resourceLoader != null) {
				loader.setResourceLoader(this.resourceLoader);
			}
			if (this.environment != null) {
				loader.setEnvironment(this.environment);
			}
			loader.load();
		}
	}

	@Substitute
	private <T> Collection<T> getSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes, Object... args) {
		ClassLoader classLoader = getClassLoader();
		// Use names and ensure unique to protect against duplicates
		Set<String> names = new LinkedHashSet<>(SpringFactoriesLoader.loadFactoryNames(type, classLoader));
		List<T> instances;
		if (args.length == 0) {
			instances = SpringFactoriesLoader.loadFactories(type, classLoader);
		} else {
			// TODO generate reflection data when args are passed
			instances = createSpringFactoriesInstances(type, parameterTypes, classLoader, args, names);
		}
		AnnotationAwareOrderComparator.sort(instances);
		return instances;
	}

	@Alias
	public ClassLoader getClassLoader() {
		return null;
	}

	@Alias
	private <T> List<T> createSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes,
			ClassLoader classLoader, Object[] args, Set<String> names) {
		return null;
	}

	@Alias
	public void setInitializers(Collection<? extends ApplicationContextInitializer<?>> initializers) {
	}

	@Alias
	private <T> Collection<T> getSpringFactoriesInstances(Class<T> type) {
		return null;
	}

	@Alias
	public void setListeners(Collection<? extends ApplicationListener<?>> listeners) {
	}

	@Alias
	private Class<?> deduceMainApplicationClass() {
		return null;
	}

	@Alias
	public void setApplicationContextFactory(ApplicationContextFactory applicationContextFactory) {
	}

	@Alias
	protected Target_BeanDefinitionLoader createBeanDefinitionLoader(BeanDefinitionRegistry registry, Object[] sources) {
		return null;
	}

	@Alias
	private BeanDefinitionRegistry getBeanDefinitionRegistry(ApplicationContext context) {
		return null;
	}
}
