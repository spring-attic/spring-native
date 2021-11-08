/*
 * Copyright 2021 the original author or authors.
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
package org.springframework.repository.annotation;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.DefaultNativeReflectionEntry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeProxyEntry;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.sample.data.beans.AtComponentAnnotatedType;
import org.springframework.sample.data.beans.AtRepositoryAnnotatedType;
import org.springframework.sample.data.beans.AtRepositoryAnnotatedTypeWithMethods;
import org.springframework.sample.data.beans.TypeInSamePackageAsRepository;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ObjectUtils;

/**
 * @author Christoph Strobl
 */
class RepositoryComponentConfigurationProcessorTests {

	@Test
	void registersProxyForAtRepositoryAnnotatedType() {

		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("someRepoComponentThatNeedsAProxy", BeanDefinitionBuilder.rootBeanDefinition(AtRepositoryAnnotatedType.class).getBeanDefinition());

		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new RepositoryComponentConfigurationProcessor().process(beanFactory, registry);

		assertThat(getProxyEntries(AtRepositoryAnnotatedType.class, registry)).hasSize(1);
	}

	@Test
	void doesNotRegisterProxyForNonAtRepositoryAnnotatedType() {

		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("someComponentThatDoesNotNeedAProxy", BeanDefinitionBuilder.rootBeanDefinition(AtComponentAnnotatedType.class).getBeanDefinition());

		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new RepositoryComponentConfigurationProcessor().process(beanFactory, registry);

		assertThat(registry.proxy().getEntries()).isEmpty();
	}

	@Test
	void registersOnlyDomainTypeInSamePackage() {

		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("repoWithDomainTypes", BeanDefinitionBuilder.rootBeanDefinition(AtRepositoryAnnotatedTypeWithMethods.class).getBeanDefinition());

		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new RepositoryComponentConfigurationProcessor().process(beanFactory, registry);

		assertThat(registry.reflection().reflectionEntries()).<Class<?>>map(DefaultNativeReflectionEntry::getType)
				.containsExactly(TypeInSamePackageAsRepository.class);
	}

	public List<NativeProxyEntry> getProxyEntries(Class<?> type, NativeConfigurationRegistry registry) {
		return registry.proxy().getEntries().stream()
				.filter(it -> {
					Object targetClass = ReflectionTestUtils.getField(it, "targetClass");
					if (ObjectUtils.nullSafeEquals(targetClass, type)) {
						return true;
					}
					List<Class<?>> interfaces = (List<Class<?>>) ReflectionTestUtils.getField(it, "interfaceTypes");
					if (ObjectUtils.isEmpty(interfaces)) {
						return false;
					}
					return ObjectUtils.nullSafeEquals(interfaces.iterator().next(), type);
				}).collect(Collectors.toList());
	}
}
