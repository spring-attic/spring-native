package org.springframework.data;

import org.junit.jupiter.api.Test;

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
	}

	@Test
	void resolveRepositoryTypeWithTypeAsUnsupportedType() {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) BeanDefinitionBuilder.rootBeanDefinition(JpaRepositoryFactoryBean.class)
				.addConstructorArgValue(42).getBeanDefinition();
		assertThat(beanDefinition.getResolvableType().hasUnresolvableGenerics()).isTrue();
		postProcess(beanDefinition);
		assertThat(beanDefinition.getResolvableType().hasUnresolvableGenerics()).isTrue();
	}

	@Test
	void resolveRepositoryTypeWithoutType() {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) BeanDefinitionBuilder.rootBeanDefinition(JpaRepositoryFactoryBean.class)
				.getBeanDefinition();
		assertThat(beanDefinition.getResolvableType().hasUnresolvableGenerics()).isTrue();
		postProcess(beanDefinition);
		assertThat(beanDefinition.getResolvableType().hasUnresolvableGenerics()).isTrue();
	}

	@Test
	void resolveRepositoryTypeWithUnrelatedTarget() {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) BeanDefinitionBuilder.rootBeanDefinition(String.class)
				.getBeanDefinition();
		postProcess(beanDefinition);
		assertThat(beanDefinition.getResolvableType().resolve()).isEqualTo(String.class);
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
