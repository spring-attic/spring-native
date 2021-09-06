package org.springframework.context.bootstrap.generator.bean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptorFactory;
import org.springframework.context.bootstrap.generator.bean.descriptor.DefaultBeanInstanceDescriptorFactory;

/**
 * Base {@link BeanRegistrationWriterSupplier} implementation taking care of locating
 * a suitable {@link BeanValueWriter}.
 *
 * @author Stephane Nicoll
 */
public abstract class AbstractBeanRegistrationWriterSupplier implements BeanRegistrationWriterSupplier, BeanFactoryAware {

	private BeanInstanceDescriptorFactory beanInstanceDescriptorFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanInstanceDescriptorFactory = new DefaultBeanInstanceDescriptorFactory((ConfigurableBeanFactory) beanFactory);
	}

	@Override
	public BeanRegistrationWriter get(String beanName, BeanDefinition beanDefinition) {
		BeanValueWriter beanValueWriter = createBeanValueWriter(beanDefinition);
		return (beanValueWriter != null) ? createInstance(beanName, beanDefinition, beanValueWriter) : null;
	}

	/**
	 * Create a {@link BeanRegistrationWriter} based on the resolved {@link BeanValueWriter}.
	 * @param beanName the name of the bean
	 * @param beanDefinition the bean definition
	 * @param beanValueWriter the bean value writer (never {@code null})
	 * @return a bean registration writer for the specified bean definition
	 */
	protected abstract BeanRegistrationWriter createInstance(String beanName, BeanDefinition beanDefinition,
			BeanValueWriter beanValueWriter);

	/**
	 * Initialize a builder for the {@link BeanRegistrationWriterOptions}.
	 * @return a builder with sensible defaults
	 */
	protected BeanRegistrationWriterOptions.Builder initializeOptions() {
		return BeanRegistrationWriterOptions.builder().withWriterFactory(this::get);
	}

	private BeanValueWriter createBeanValueWriter(BeanDefinition beanDefinition) {
		BeanInstanceDescriptor descriptor = this.beanInstanceDescriptorFactory.create(beanDefinition);
		return (descriptor != null) ? new DefaultBeanValueWriter(descriptor, beanDefinition) : null;
	}
}
