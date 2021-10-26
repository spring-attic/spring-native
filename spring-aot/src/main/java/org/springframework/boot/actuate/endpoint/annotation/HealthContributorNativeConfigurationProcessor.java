package org.springframework.boot.actuate.endpoint.annotation;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanFactoryNativeConfigurationProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.nativex.hint.Flag;
import org.springframework.util.ClassUtils;

/**
 * A {@link BeanFactoryNativeConfigurationProcessor} that register reflection access for
 * actuator health contributor.
 *
 * @author Olivier Boudet
 */

public class HealthContributorNativeConfigurationProcessor implements BeanFactoryNativeConfigurationProcessor {
    @Override
    public void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
        if (ClassUtils.isPresent("org.springframework.boot.actuate.health.HealthContributor", beanFactory.getBeanClassLoader())) {
            String[] healthContributorBeanNames = beanFactory.getBeanNamesForType(HealthContributor.class);
            for (String bean : healthContributorBeanNames) {
                registry.reflection().forType(beanFactory.getBean(bean, HealthContributor.class).getClass()).withFlags(Flag.allDeclaredConstructors).build();
            }
        }
    }
}
