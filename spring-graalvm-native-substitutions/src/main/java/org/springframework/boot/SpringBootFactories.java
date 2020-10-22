package org.springframework.boot;

import java.util.Arrays;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationImportListener;
import org.springframework.boot.autoconfigure.condition.ConditionProvider;
import org.springframework.boot.autoconfigure.flyway.FlywayProvider;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerTemplateAvailabilityProvider;
import org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAvailabilityProvider;
import org.springframework.boot.autoconfigure.jdbc.JdbcProvider;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.autoconfigure.mustache.MustacheTemplateAvailabilityProvider;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProvider;
import org.springframework.boot.autoconfigure.session.SessionProvider;
import org.springframework.boot.autoconfigure.template.TemplateAvailabilityProvider;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafTemplateAvailabilityProvider;
import org.springframework.boot.autoconfigure.web.servlet.JspTemplateAvailabilityProvider;
import org.springframework.boot.builder.ParentContextCloserApplicationListener;
import org.springframework.boot.context.ConfigurationWarningsApplicationContextInitializer;
import org.springframework.boot.context.ContextIdApplicationContextInitializer;
import org.springframework.boot.context.FileEncodingApplicationListener;
import org.springframework.boot.context.config.AnsiOutputApplicationListener;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.context.config.ConfigDataLocationResolver;
import org.springframework.boot.context.config.ConfigTreeConfigDataLoader;
import org.springframework.boot.context.config.ConfigTreeConfigDataLocationResolver;
import org.springframework.boot.context.config.DelegatingApplicationContextInitializer;
import org.springframework.boot.context.config.DelegatingApplicationListener;
import org.springframework.boot.context.config.StandardConfigDataLoader;
import org.springframework.boot.context.config.StandardConfigDataLocationResolver;
import org.springframework.boot.context.logging.ClasspathLoggingApplicationListener;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.diagnostics.FailureAnalysisReporter;
import org.springframework.boot.diagnostics.FailureAnalyzer;
import org.springframework.boot.diagnostics.LoggingFailureAnalysisReporter;
import org.springframework.boot.diagnostics.analyzer.AnalyzerProvider;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessorApplicationListener;
import org.springframework.boot.env.EnvironmentPostProcessorsFactory;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.boot.env.RandomValuePropertySourceEnvironmentPostProcessor;
import org.springframework.boot.env.SpringApplicationJsonEnvironmentPostProcessor;
import org.springframework.boot.env.SystemEnvironmentPropertySourceEnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.liquibase.LiquibaseServiceLocatorApplicationListener;
import org.springframework.boot.logging.LoggingSystemFactory;
import org.springframework.boot.logging.java.JavaLoggingSystem;
import org.springframework.boot.logging.log4j2.Log4J2LoggingSystem;
import org.springframework.boot.logging.logback.LogbackLoggingSystem;
import org.springframework.boot.rsocket.context.RSocketPortInfoApplicationContextInitializer;
import org.springframework.boot.web.context.ServerPortInfoApplicationContextInitializer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.init.func.FunctionalInstallerListener;
import org.springframework.init.func.InfrastructureInitializer;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public abstract class SpringBootFactories {

	public static MultiValueMap<Class<?>, Object> factories = new LinkedMultiValueMap<>();

	static {
		boolean removeYamlSupport = Boolean.valueOf(System.getProperty("spring.native.remove-yaml-support", "false"));
		boolean isAutoconfigurePresent = ClassUtils.isPresent(
				"org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener", null);
		boolean isRsocketPresent = ClassUtils.isPresent("io.rsocket.RSocket", null);
		boolean isLiquibasePresent = ClassUtils.isPresent("liquibase.servicelocator.CustomResolverServiceLocator",
				null);
		boolean isFlywayPresent = ClassUtils.isPresent("org.flywaydb.core.Flyway", null);
		boolean isFreemarkerPresent = ClassUtils.isPresent("freemarker.template.Configuration", null);
		boolean isMustachePresent = ClassUtils.isPresent("com.samskivert.mustache.Mustache", null);
		boolean isGroovyTemplatePresent = ClassUtils.isPresent("groovy.text.TemplateEngine", null);
		boolean isThymeleafPresent = ClassUtils.isPresent("org.thymeleaf.spring5.SpringTemplateEngine", null);
		boolean isJspPresent = ClassUtils.isPresent("org.apache.jasper.compiler.JspConfig", null);
		boolean isSpringJdbcPresent = ClassUtils.isPresent("org.springframework.jdbc.CannotGetJdbcConnectionException",
				null);
		boolean isR2dbcPresent = ClassUtils.isPresent("io.r2dbc.spi.ConnectionFactory", null);
		boolean isSpringInitPresent = ClassUtils.isPresent("org.springframework.init.func.FunctionalInstallerListener",
				null);
		boolean isLogbackPresent = ClassUtils.isPresent("ch.qos.logback.core.Appender", null);
		boolean isLog4j2Present = ClassUtils.isPresent("org.apache.logging.log4j.core.impl.Log4jContextFactory", null);
		boolean isJavaLoggingPresent = ClassUtils.isPresent("java.util.logging.LogManager", null);

		// LoggingSystemFactory
		if (isLogbackPresent) {
			factories.add(LoggingSystemFactory.class, new LogbackLoggingSystem.Factory());
		}
		if (isLog4j2Present) {
			factories.add(LoggingSystemFactory.class, new Log4J2LoggingSystem.Factory());
		}
		if (isJavaLoggingPresent) {
			factories.add(LoggingSystemFactory.class, new JavaLoggingSystem.Factory());
		}

		// PropertySourceLoader
		factories.add(PropertySourceLoader.class, new PropertiesPropertySourceLoader());
		if (!removeYamlSupport) {
			factories.add(PropertySourceLoader.class, new YamlPropertySourceLoader());
		}

		// ConfigData Location Resolvers
		factories.add(ConfigDataLocationResolver.class, new ConfigTreeConfigDataLocationResolver());

		// ConfigData Loaders
		factories.add(ConfigDataLoader.class, new ConfigTreeConfigDataLoader());
		factories.add(ConfigDataLoader.class, new StandardConfigDataLoader());

		// ApplicationContextInitializer
		factories.add(ApplicationContextInitializer.class, new ConfigurationWarningsApplicationContextInitializer());
		factories.add(ApplicationContextInitializer.class, new ContextIdApplicationContextInitializer());
		factories.add(ApplicationContextInitializer.class, new DelegatingApplicationContextInitializer());
		if (isRsocketPresent) {
			factories.add(ApplicationContextInitializer.class, new RSocketPortInfoApplicationContextInitializer());
		}
		factories.add(ApplicationContextInitializer.class, new ServerPortInfoApplicationContextInitializer());
		if (isAutoconfigurePresent) {
			// factories.add(ApplicationContextInitializer.class, AutoconfigureProvider.getSharedMetadataReaderFactoryContextInitializer());
			factories.add(ApplicationContextInitializer.class, new ConditionEvaluationReportLoggingListener());
		}

		EnvironmentPostProcessorsFactory environmentPostProcessorsFactory = (logFactory, bootstrapContext) -> {
			// EnvironmentPostProcessor
			return Arrays.asList(
					// new CloudFoundryVcapEnvironmentPostProcessor()
					new ConfigDataEnvironmentPostProcessor(logFactory, new DefaultBootstrapContext()) {
						@Override
						public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
							factories.add(ConfigDataLocationResolver.class, new StandardConfigDataLocationResolver(logFactory.getLog(ConfigDataLocationResolver.class), Binder.get(environment), application.getResourceLoader()));
							super.postProcessEnvironment(environment, application);
						}
					},
					new RandomValuePropertySourceEnvironmentPostProcessor(logFactory.getLog(EnvironmentPostProcessor.class)),
					new SpringApplicationJsonEnvironmentPostProcessor(),
					new SystemEnvironmentPropertySourceEnvironmentPostProcessor()
					// new DebugAgentEnvironmentPostProcessor()
			);
		};

		// ApplicationListener
		// factories.add(ApplicationListener.class, new
		// ClearCachesApplicationListener());
		factories.add(ApplicationListener.class,
				new EnvironmentPostProcessorApplicationListener(environmentPostProcessorsFactory));
		factories.add(ApplicationListener.class, new ParentContextCloserApplicationListener());
		// factories.add(ApplicationListener.class, new
		// CloudFoundryVcapEnvironmentPostProcessor());
		factories.add(ApplicationListener.class, new FileEncodingApplicationListener());
		factories.add(ApplicationListener.class, new AnsiOutputApplicationListener());
		factories.add(ApplicationListener.class, new DelegatingApplicationListener());
		factories.add(ApplicationListener.class, new ClasspathLoggingApplicationListener());
		factories.add(ApplicationListener.class, new LoggingApplicationListener());
		if (isLiquibasePresent) {
			factories.add(ApplicationListener.class, new LiquibaseServiceLocatorApplicationListener());
		}
		// No BackgroundPreinitializer, makes no sense with native images
		if (isSpringInitPresent) {
			factories.add(ApplicationContextInitializer.class, new InfrastructureInitializer());
			factories.add(ApplicationListener.class, new FunctionalInstallerListener());
		}
		factories.add(ApplicationListener.class, new NativePropertiesListener());

		// FailureAnalyzer
		factories.add(FailureAnalyzer.class, AnalyzerProvider.getBeanCurrentlyInCreationFailureAnalyzer());
		factories.add(FailureAnalyzer.class, AnalyzerProvider.getBeanDefinitionOverrideFailureAnalyzer());
		factories.add(FailureAnalyzer.class, AnalyzerProvider.getBeanNotOfRequiredTypeFailureAnalyzer());
		factories.add(FailureAnalyzer.class, AnalyzerProvider.getBindFailureAnalyzer());
		factories.add(FailureAnalyzer.class, AnalyzerProvider.getBindValidationFailureAnalyzer());
		factories.add(FailureAnalyzer.class, AnalyzerProvider.getUnboundConfigurationPropertyFailureAnalyzer());
		factories.add(FailureAnalyzer.class, AnalyzerProvider.getConnectorStartFailureAnalyzer());
		factories.add(FailureAnalyzer.class, AnalyzerProvider.getNoSuchMethodFailureAnalyzer());
		factories.add(FailureAnalyzer.class, AnalyzerProvider.getNoUniqueBeanDefinitionFailureAnalyzer());
		factories.add(FailureAnalyzer.class, AnalyzerProvider.getPortInUseFailureAnalyzer());
		// TODO Class not found exception when uncommented, maybe a native-image static
		// analysis bug
		// factories.add(FailureAnalyzer.class,
		// AnalyzerProvider.getValidationExceptionFailureAnalyzer());
		factories.add(FailureAnalyzer.class, AnalyzerProvider.getInvalidConfigurationPropertyNameFailureAnalyzer());
		factories.add(FailureAnalyzer.class, AnalyzerProvider.getInvalidConfigurationPropertyValueFailureAnalyzer());

		if (isAutoconfigurePresent) {
			// No NoSuchBeanDefinitionFailureAnalyzer since it triggers usage of
			// org.springframework.core.type.classreading stuff
			// factories.add(FailureAnalyzer.class,
			// AutoconfigureAnalyzerProvider.getNoSuchBeanDefinitionFailureAnalyzer());
			if (isFlywayPresent) {
				factories.add(FailureAnalyzer.class, FlywayProvider.getFlywayMigrationScriptMissingFailureAnalyzer());
			}
			if (isSpringJdbcPresent) {
				factories.add(FailureAnalyzer.class, JdbcProvider.getDataSourceBeanCreationFailureAnalyzer());
				factories.add(FailureAnalyzer.class, JdbcProvider.getHikariDriverConfigurationFailureAnalyzer());
			}
			if (isR2dbcPresent) {
				factories.add(FailureAnalyzer.class, R2dbcProvider.getConnectionFactoryBeanCreationFailureAnalyzer());
			}
			factories.add(FailureAnalyzer.class, SessionProvider.getNonUniqueSessionRepositoryFailureAnalyzer());
		}

		// FailureAnalysisReporter
		factories.add(FailureAnalysisReporter.class, new LoggingFailureAnalysisReporter());

		if (isAutoconfigurePresent) {
			// AutoConfigurationImportListener
			factories.add(AutoConfigurationImportListener.class,
					ConditionProvider.getConditionEvaluationReportAutoConfigurationImportListener());

			// AutoConfigurationImportFilter
			factories.add(AutoConfigurationImportFilter.class, ConditionProvider.getOnBeanCondition());
			factories.add(AutoConfigurationImportFilter.class, ConditionProvider.getOnClassCondition());
			factories.add(AutoConfigurationImportFilter.class, ConditionProvider.getOnWebApplicationCondition());

			// TemplateAvailabilityProvider
			if (isFreemarkerPresent) {
				factories.add(TemplateAvailabilityProvider.class, new FreeMarkerTemplateAvailabilityProvider());
			}
			if (isMustachePresent) {
				factories.add(TemplateAvailabilityProvider.class, new MustacheTemplateAvailabilityProvider());
			}
			if (isGroovyTemplatePresent) {
				factories.add(TemplateAvailabilityProvider.class, new GroovyTemplateAvailabilityProvider());
			}
			if (isThymeleafPresent) {
				factories.add(TemplateAvailabilityProvider.class, new ThymeleafTemplateAvailabilityProvider());
			}
			if (isJspPresent) {
				factories.add(TemplateAvailabilityProvider.class, new JspTemplateAvailabilityProvider());
			}
		}
	}
}
