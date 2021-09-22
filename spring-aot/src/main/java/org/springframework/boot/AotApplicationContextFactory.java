package org.springframework.boot;

import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.boot.logging.DeferredLogs;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ResourceLoader;

/**
 * Prepare an application context for AOT processing.
 *
 * @author Brian Clozel
 * @author Stephane Nicoll
 */
public final class AotApplicationContextFactory {

	private final ResourceLoader resourceLoader;

	public AotApplicationContextFactory(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	/**
	 * Create a {@link GenericApplicationContext} suitable for the specified
	 * {@code applicationClass}.
	 * @param applicationClass an application class to be used as the root of the context
	 * @return an application context whose environment has been loaded, but not refreshed
	 */
	public GenericApplicationContext createApplicationContext(Class<?> applicationClass) {
		SpringApplication application = new SpringApplication(this.resourceLoader, applicationClass);
		ConfigurableEnvironment environment = loadEnvironment(application);
		GenericApplicationContext applicationContext = createApplicationContext(application);
		applicationContext.setResourceLoader(this.resourceLoader);
		applicationContext.setEnvironment(environment);
		applicationContext.registerBean(applicationClass);
		return applicationContext;
	}

	private ConfigurableEnvironment loadEnvironment(SpringApplication application) {
		ConfigurableEnvironment environment = getOrCreateEnvironment(application.getWebApplicationType());
		environment.setConversionService(new ApplicationConversionService());
		ConfigurationPropertySources.attach(environment);
		ConfigDataEnvironmentPostProcessor configDataEnvironmentPostProcessor =
				new ConfigDataEnvironmentPostProcessor(new DeferredLogs(), new DefaultBootstrapContext());
		configDataEnvironmentPostProcessor.postProcessEnvironment(environment, application);
		bindToSpringApplication(environment, application);
		environment = new EnvironmentConverter(this.resourceLoader.getClassLoader()).convertEnvironmentIfNecessary(
				environment, deduceEnvironmentClass(application.getWebApplicationType()));
		return environment;
	}

	private GenericApplicationContext createApplicationContext(SpringApplication application) {
		return (GenericApplicationContext) ApplicationContextFactory.DEFAULT.create(application.getWebApplicationType());
	}

	private static ConfigurableEnvironment getOrCreateEnvironment(WebApplicationType webApplicationType) {
		switch (webApplicationType) {
			case SERVLET:
				return new ApplicationServletEnvironment();
			case REACTIVE:
				return new ApplicationReactiveWebEnvironment();
			default:
				return new ApplicationEnvironment();
		}
	}

	private static Class<? extends StandardEnvironment> deduceEnvironmentClass(WebApplicationType webApplicationType) {
		switch (webApplicationType) {
			case SERVLET:
				return ApplicationServletEnvironment.class;
			case REACTIVE:
				return ApplicationReactiveWebEnvironment.class;
			default:
				return ApplicationEnvironment.class;
		}
	}

	private void bindToSpringApplication(ConfigurableEnvironment environment, SpringApplication application) {
		try {
			Binder.get(environment).bind("spring.main", Bindable.ofInstance(application));
		}
		catch (Exception ex) {
			throw new IllegalStateException("Cannot bind to SpringApplication", ex);
		}
	}

}
