package org.springframework.context.bootstrap.generator.infrastructure.nativex;

import java.util.List;

import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * Register native configuration using processors detected on the classpath. Both
 * {@link BeanFactoryNativeConfigurationProcessor} and
 * {@link BeanNativeConfigurationProcessor} are handled.
 *
 * @author Stephane Nicoll
 */
public class NativeConfigurationRegistrar {

	private final ConfigurableListableBeanFactory beanFactory;

	private final List<BeanFactoryNativeConfigurationProcessor> beanFactoryProcessors;

	NativeConfigurationRegistrar(ConfigurableListableBeanFactory beanFactory,
			List<BeanFactoryNativeConfigurationProcessor> beanFactoryProcessors) {
		this.beanFactory = beanFactory;
		this.beanFactoryProcessors = beanFactoryProcessors;
	}

	public NativeConfigurationRegistrar(ConfigurableListableBeanFactory beanFactory) {
		this(beanFactory, SpringFactoriesLoader.loadFactories(
				BeanFactoryNativeConfigurationProcessor.class, beanFactory.getBeanClassLoader()));
	}

	/**
	 * Process the bean factory against the specified {@link NativeConfigurationRegistry}.
	 * @param registry the registry to use
	 */
	public void processBeanFactory(NativeConfigurationRegistry registry) {
		this.beanFactoryProcessors.forEach((processor) -> processor.process(this.beanFactory, registry));
	}

	/**
	 * Process the {@link BeanInstanceDescriptor bean instance descriptors} against the
	 * specified {@link NativeConfigurationRegistry}.
	 * @param registry the registry to use
	 * @param beans the bean instance descriptors
	 */
	public void processBeans(NativeConfigurationRegistry registry, Iterable<BeanInstanceDescriptor> beans) {
		List<BeanNativeConfigurationProcessor> beanProcessors = loadBeanProcessors(this.beanFactory);
		beanProcessors.forEach((processor) -> beans.forEach((bean) -> processor.process(bean, registry)));
	}

	protected List<BeanNativeConfigurationProcessor> loadBeanProcessors(ConfigurableListableBeanFactory beanFactory) {
		List<BeanNativeConfigurationProcessor> processors = SpringFactoriesLoader.loadFactories(
				BeanNativeConfigurationProcessor.class, beanFactory.getBeanClassLoader());
		for (BeanNativeConfigurationProcessor processor : processors) {
			if (processor instanceof BeanFactoryAware) {
				((BeanFactoryAware) processor).setBeanFactory(beanFactory);
			}
		}
		return processors;
	}

}
