/*
 * Copyright 2019-2022 the original author or authors.
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

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import org.springframework.aot.context.bootstrap.generator.bean.BeanRegistrationWriter;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

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

	@Test
	void resolveRepositoryTypeWithSpecificFactoryBean() {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) BeanDefinitionBuilder
				.rootBeanDefinition(SpecificJpaRepositoryFactoryBean.class)
				.addConstructorArgValue(SpeakerRepository.class).getBeanDefinition();
		assertThat(beanDefinition.getResolvableType().hasUnresolvableGenerics()).isTrue();
		postProcess(beanDefinition);
		ResolvableType resolvedType = beanDefinition.getResolvableType();
		assertThat(resolvedType.hasUnresolvableGenerics()).isFalse();
		assertThat(resolvedType.getGenerics()).satisfies(ofTypes(Speaker.class, Integer.class));
		assertThat(beanDefinition.getAttribute(BeanRegistrationWriter.PRESERVE_TARGET_TYPE)).isEqualTo(true);
	}

	@Test
	void resolveRepositoryTypeWithSpecificPrimaryKey() {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) BeanDefinitionBuilder
				.rootBeanDefinition(SpecificPrimaryKeyFactoryBean.class)
				.addConstructorArgValue(SpeakerRepository.class).getBeanDefinition();
		assertThat(beanDefinition.getResolvableType().hasUnresolvableGenerics()).isTrue();
		postProcess(beanDefinition);
		ResolvableType resolvedType = beanDefinition.getResolvableType();
		assertThat(resolvedType.hasUnresolvableGenerics()).isFalse();
		assertThat(resolvedType.getGenerics()).satisfies(ofTypes(SpeakerRepository.class, Speaker.class));
		assertThat(beanDefinition.getAttribute(BeanRegistrationWriter.PRESERVE_TARGET_TYPE)).isEqualTo(true);
	}

	@Test
	void resolveRepositoryTypeWithSpecificFactoryBeanAndPrimaryKey() {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) BeanDefinitionBuilder
				.rootBeanDefinition(SpecificJpaRepositoryAndPrimaryKeyFactoryBean.class)
				.addConstructorArgValue(SpeakerRepository.class).getBeanDefinition();
		assertThat(beanDefinition.getResolvableType().hasUnresolvableGenerics()).isTrue();
		postProcess(beanDefinition);
		ResolvableType resolvedType = beanDefinition.getResolvableType();
		assertThat(resolvedType.hasUnresolvableGenerics()).isFalse();
		assertThat(resolvedType.getGenerics()).satisfies(ofTypes(Speaker.class));
		assertThat(beanDefinition.getAttribute(BeanRegistrationWriter.PRESERVE_TARGET_TYPE)).isEqualTo(true);
	}

	@Test
	void resolveRepositoryTypeWithGenericsResolvedUpfront() {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) BeanDefinitionBuilder
				.rootBeanDefinition(SpeakerRepositoryFactoryBean.class)
				.addConstructorArgValue(SpeakerRepository.class).getBeanDefinition();
		postProcess(beanDefinition);
		ResolvableType resolvedType = beanDefinition.getResolvableType();
		assertThat(resolvedType.toClass()).isEqualTo(SpeakerRepositoryFactoryBean.class);
		assertThat(resolvedType.hasGenerics()).isFalse();
		assertThat(beanDefinition.getAttribute(BeanRegistrationWriter.PRESERVE_TARGET_TYPE)).isEqualTo(true);
	}

	private void assertFactoryBeanForSpeakerRepository(ResolvableType resolvedType) {
		assertThat(resolvedType.hasUnresolvableGenerics()).isFalse();
		assertThat(resolvedType.getGenerics()).satisfies(ofTypes(SpeakerRepository.class, Speaker.class, Integer.class));
	}

	@SuppressWarnings("rawtypes")
	private Consumer<? super ResolvableType[]> ofTypes(Class<?>... classes) {
		return (types) -> assertThat(types).extracting((type) -> (Class) type.toClass())
				.containsExactly(classes);
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


	static class SpecificJpaRepositoryFactoryBean<T, ID> extends JpaRepositoryFactoryBean<JpaRepository<T, ID>, T, ID> {

		public SpecificJpaRepositoryFactoryBean(Class<JpaRepository<T, ID>> repositoryInterface) {
			super(repositoryInterface);
		}
	}

	static class SpecificPrimaryKeyFactoryBean<T extends Repository<E, Long>, E> extends JpaRepositoryFactoryBean<T, E, Long> {

		public SpecificPrimaryKeyFactoryBean(Class<T> repositoryInterface) {
			super(repositoryInterface);
		}
	}

	static class SpecificJpaRepositoryAndPrimaryKeyFactoryBean<E> extends JpaRepositoryFactoryBean<JpaRepository<E, Long>, E, Long> {

		public SpecificJpaRepositoryAndPrimaryKeyFactoryBean(Class<JpaRepository<E, Long>> repositoryInterface) {
			super(repositoryInterface);
		}
	}

	static class SpeakerRepositoryFactoryBean extends JpaRepositoryFactoryBean<SpeakerRepository, Speaker, Integer> {

		public SpeakerRepositoryFactoryBean() {
			super(SpeakerRepository.class);
		}
	}

}
