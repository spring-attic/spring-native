package org.springframework.data;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.BeanDefinitionPostProcessor;
import org.springframework.core.ResolvableType;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.util.ClassUtils;

/**
 * A {@link BeanDefinitionPostProcessor} that enriches the target type of a Spring Data
 * repository with the resolved generics of the repository.
 *
 * @author Stephane Nicoll
 */
class RepositoryFactoryBeanPostProcessor implements BeanDefinitionPostProcessor, BeanFactoryAware {

	private static final String REPOSITORY_SUPPORT = "org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport";

	private ConfigurableBeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (ConfigurableBeanFactory) beanFactory;
	}

	@Override
	public void postProcessBeanDefinition(String beanName, RootBeanDefinition beanDefinition) {
		if (ClassUtils.isPresent(REPOSITORY_SUPPORT, this.beanFactory.getBeanClassLoader())) {
			resolveRepositoryFactoryBeanTypeIfNecessary(beanDefinition);
		}
	}

	private void resolveRepositoryFactoryBeanTypeIfNecessary(RootBeanDefinition beanDefinition) {
		if (!beanDefinition.hasBeanClass() || !RepositoryFactoryBeanSupport.class.isAssignableFrom(beanDefinition.getBeanClass())) {
			return;
		}
		ValueHolder valueHolder = beanDefinition.getConstructorArgumentValues().getIndexedArgumentValue(0, null);
		Class<?> repositoryType = loadRepositoryType(valueHolder);
		if (repositoryType != null) {
			ResolvableType resolvableType = ResolvableType.forClass(repositoryType).as(Repository.class);
			ResolvableType entityType = resolvableType.getGenerics()[0];
			ResolvableType idType = resolvableType.getGenerics()[1];
			ResolvableType resolvedRepositoryType = ResolvableType.forClassWithGenerics(
					beanDefinition.getBeanClass(), ResolvableType.forClass(repositoryType), entityType, idType);
			beanDefinition.setTargetType(resolvedRepositoryType);
		}
	}

	private Class<?> loadRepositoryType(ValueHolder valueHolder) {
		if (valueHolder == null) {
			return null;
		}
		Object value = valueHolder.getValue();
		if (value instanceof Class) {
			return (Class<?>) value;
		}
		if (value instanceof String) {
			try {
				return ClassUtils.forName((String) value, this.beanFactory.getBeanClassLoader());
			}
			catch (ClassNotFoundException ex) {
				throw new IllegalStateException("Failed to load repository type " + value, ex);
			}
		}
		return null;
	}

}
