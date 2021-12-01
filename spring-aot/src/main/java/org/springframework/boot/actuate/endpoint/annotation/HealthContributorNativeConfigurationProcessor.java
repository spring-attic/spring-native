/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.actuate.endpoint.annotation;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanFactoryNativeConfigurationProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.support.BeanFactoryProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.actuate.autoconfigure.health.AbstractCompositeHealthContributorConfiguration;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.core.ResolvableType;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.util.ClassUtils;

/**
 * A {@link BeanFactoryNativeConfigurationProcessor} that register reflection access for
 * actuator health contributor.
 *
 * @author Olivier Boudet
 * @author Stephane Nicoll
 */
class HealthContributorNativeConfigurationProcessor implements BeanFactoryNativeConfigurationProcessor {

	private static final String HEALTH_CONTRIBUTOR_CLASS_NAME = "org.springframework.boot.actuate.health.HealthContributor";

	private static final String COMPOSITE_HEALTH_CONTRIBUTOR_CLASS_NAME = "org.springframework.boot.actuate.autoconfigure.health.AbstractCompositeHealthContributorConfiguration";

	@Override
	public void process(ConfigurableListableBeanFactory beanFactory, NativeConfigurationRegistry registry) {
		BeanFactoryProcessor processor = new BeanFactoryProcessor(beanFactory);
		if (ClassUtils.isPresent(HEALTH_CONTRIBUTOR_CLASS_NAME, beanFactory.getBeanClassLoader())) {
			processor.processBeansWithType(HealthContributor.class, (beanName, beanType) -> {
				if (!beanType.isInterface()) {
					registry.reflection().forType(beanType).withAccess(TypeAccess.DECLARED_CONSTRUCTORS).build();
				}
			});
		}
		if (ClassUtils.isPresent(COMPOSITE_HEALTH_CONTRIBUTOR_CLASS_NAME, beanFactory.getBeanClassLoader())) {
			processor.processBeansWithType(AbstractCompositeHealthContributorConfiguration.class, (beanName, beanType) -> {
				ResolvableType type = ResolvableType.forClass(AbstractCompositeHealthContributorConfiguration.class,
						beanType);
				Class<?> indicatorType = type.resolveGeneric(1);
				registry.reflection().forType(indicatorType).withAccess(TypeAccess.DECLARED_CONSTRUCTORS).build();
			});
		}
	}

}
