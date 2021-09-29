package org.springframework.boot;

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

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.nativex.AotModeDetector;
import org.springframework.nativex.substitutions.OnlyIfPresent;
import org.springframework.util.StringUtils;

@TargetClass(className = "org.springframework.boot.SpringApplication", onlyWith = { OnlyIfPresent.class })
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
		this.webApplicationType = WebApplicationType.deduceFromClasspath();
		this.bootstrapRegistryInitializers = (List<BootstrapRegistryInitializer>) getSpringFactoriesInstances(BootstrapRegistryInitializer.class);
		setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
		this.mainApplicationClass = deduceMainApplicationClass();

		if (AotModeDetector.isAotModeEnabled()) {
			logger.info("AOT mode enabled");
			setApplicationContextFactory(SpringApplicationAotUtils.AOT_FACTORY);
			List<ApplicationContextInitializer<?>> initializers = new ArrayList<>();
			initializers.add(SpringApplicationAotUtils.getBootstrapInitializer());
			initializers.add(new ConditionEvaluationReportLoggingListener());
			initializers.addAll((Collection)getSpringFactoriesInstances(ApplicationContextInitializer.class));
			setInitializers(initializers);
		}
		else {
			logger.info("AOT mode disabled");
			setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
		}
	}

	@Substitute
	protected void load(ApplicationContext context, Object[] sources) {
		if (!AotModeDetector.isAotModeEnabled()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Loading source " + StringUtils.arrayToCommaDelimitedString(sources));
			}
			BeanDefinitionLoader loader = createBeanDefinitionLoader(getBeanDefinitionRegistry(context), sources);
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
	protected BeanDefinitionLoader createBeanDefinitionLoader(BeanDefinitionRegistry registry, Object[] sources) {
		return null;
	}

	@Alias
	private BeanDefinitionRegistry getBeanDefinitionRegistry(ApplicationContext context) {
		return null;
	}
}
