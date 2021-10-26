package org.springframework.boot.actuate.endpoint.annotation;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanFactoryNativeConfigurationProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.nativex.hint.Flag;
import org.springframework.util.ClassUtils;

/**
 * A {@link BeanFactoryNativeConfigurationProcessor} that register reflection access for
 * actuator health contributor.
 *
 * @author Olivier Boudet
 */
class HealthContributorNativeConfigurationProcessor implements BeanFactoryNativeConfigurationProcessor {

	private static final String HEALTH_CONTRIBUTOR_CLASS_NAME = "org.springframework.boot.actuate.health.HealthContributor";

	@Override
	public void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
		if (ClassUtils.isPresent(HEALTH_CONTRIBUTOR_CLASS_NAME, beanFactory.getBeanClassLoader())) {
			String[] beanNames = beanFactory.getBeanNamesForType(HealthContributor.class);
			for (String beanName : beanNames) {
				Class<?> beanType = beanFactory.getBeanDefinition(beanName).getResolvableType().toClass();
				registry.reflection().forType(beanType).withFlags(Flag.allDeclaredConstructors).build();
			}
		}
	}
}
