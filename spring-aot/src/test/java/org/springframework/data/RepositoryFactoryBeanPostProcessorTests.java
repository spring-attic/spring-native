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

package org.springframework.data;

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.bean.BeanRegistrationWriter;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.CrudRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Tests for {@link RepositoryFactoryBeanPostProcessor}.
 *
 * @author Stephane Nicoll
 */
class RepositoryFactoryBeanPostProcessorTests {

	@Test
	void resolveRepositoryTypeWithTypeAsString() {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) BeanDefinitionBuilder.rootBeanDefinition(JpaRepositoryFactoryBean.class)
				.addConstructorArgValue("org.springframework.data.RepositoryFactoryBeanPostProcessorTests.SpeakerRepository").getBeanDefinition();
		assertThat(beanDefinition.getResolvableType().hasUnresolvableGenerics()).isTrue();
		postProcess(beanDefinition);
		assertFactoryBeanForSpeakerRepository(beanDefinition.getResolvableType());
		assertThat(beanDefinition.getAttribute(BeanRegistrationWriter.PRESERVE_TARGET_TYPE)).isEqualTo(true);
	}

	@Test
	void resolveRepositoryTypeWithTypeAsStringThatDoesNotExist() {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) BeanDefinitionBuilder.rootBeanDefinition(JpaRepositoryFactoryBean.class)
				.addConstructorArgValue("does-not-exist").getBeanDefinition();
		assertThatIllegalStateException().isThrownBy(() -> postProcess(beanDefinition))
				.withCauseInstanceOf(ClassNotFoundException.class);
	}

	@Test
	void resolveRepositoryTypeWithTypeAsClass() {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) BeanDefinitionBuilder.rootBeanDefinition(JpaRepositoryFactoryBean.class)
				.addConstructorArgValue(SpeakerRepository.class).getBeanDefinition();
		assertThat(beanDefinition.getResolvableType().hasUnresolvableGenerics()).isTrue();
		postProcess(beanDefinition);
		assertFactoryBeanForSpeakerRepository(beanDefinition.getResolvableType());
		assertThat(beanDefinition.getAttribute(BeanRegistrationWriter.PRESERVE_TARGET_TYPE)).isEqualTo(true);
	}

	@Test
	void resolveRepositoryTypeWithTypeAsUnsupportedType() {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) BeanDefinitionBuilder.rootBeanDefinition(JpaRepositoryFactoryBean.class)
				.addConstructorArgValue(42).getBeanDefinition();
		assertThat(beanDefinition.getResolvableType().hasUnresolvableGenerics()).isTrue();
		postProcess(beanDefinition);
		assertThat(beanDefinition.getResolvableType().hasUnresolvableGenerics()).isTrue();
		assertThat(beanDefinition.hasAttribute(BeanRegistrationWriter.PRESERVE_TARGET_TYPE)).isFalse();
	}

	@Test
	void resolveRepositoryTypeWithoutType() {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) BeanDefinitionBuilder.rootBeanDefinition(JpaRepositoryFactoryBean.class)
				.getBeanDefinition();
		assertThat(beanDefinition.getResolvableType().hasUnresolvableGenerics()).isTrue();
		postProcess(beanDefinition);
		assertThat(beanDefinition.getResolvableType().hasUnresolvableGenerics()).isTrue();
		assertThat(beanDefinition.hasAttribute(BeanRegistrationWriter.PRESERVE_TARGET_TYPE)).isFalse();
	}

	@Test
	void resolveRepositoryTypeWithUnrelatedTarget() {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) BeanDefinitionBuilder.rootBeanDefinition(String.class)
				.getBeanDefinition();
		postProcess(beanDefinition);
		assertThat(beanDefinition.getResolvableType().resolve()).isEqualTo(String.class);
		assertThat(beanDefinition.hasAttribute(BeanRegistrationWriter.PRESERVE_TARGET_TYPE)).isFalse();
	}

	@Test
	void resolveRepositoryTypeWithoutBeanClass() {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) BeanDefinitionBuilder.rootBeanDefinition("com.example.Test")
				.getBeanDefinition();
		ResolvableType initialType = beanDefinition.getResolvableType();
		postProcess(beanDefinition);
		assertThat(beanDefinition.getResolvableType()).isSameAs(initialType);
	}

	private void assertFactoryBeanForSpeakerRepository(ResolvableType resolvedType) {
		assertThat(resolvedType.hasUnresolvableGenerics()).isFalse();
		assertThat(resolvedType.getGenerics()[0].resolve()).isEqualTo(SpeakerRepository.class);
		assertThat(resolvedType.getGenerics()[1].resolve()).isEqualTo(Speaker.class);
		assertThat(resolvedType.getGenerics()[2].resolve()).isEqualTo(Integer.class);
	}

	private void postProcess(RootBeanDefinition beanDefinition) {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		RepositoryFactoryBeanPostProcessor processor = new RepositoryFactoryBeanPostProcessor();
		processor.setBeanFactory(beanFactory);
		processor.postProcessBeanDefinition("test", beanDefinition);
	}

	interface SpeakerRepository extends CrudRepository<Speaker, Integer> {

	}

	static class Speaker {

	}

}
