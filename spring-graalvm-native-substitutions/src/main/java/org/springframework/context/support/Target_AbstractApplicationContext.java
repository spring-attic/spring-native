package org.springframework.context.support;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.support.ResourceEditorRegistrar;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.graalvm.substitutions.OnlyPresent;
import org.springframework.graalvm.substitutions.RemoveSpelSupport;
import org.springframework.util.ClassUtils;

@TargetClass(className = "org.springframework.context.support.AbstractApplicationContext", onlyWith = { OnlyPresent.class, RemoveSpelSupport.class })
final class Target_AbstractApplicationContext {

	@Substitute
	protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		// Tell the internal bean factory to use the context's class loader etc.
		beanFactory.setBeanClassLoader(ClassUtils.getDefaultClassLoader()); // Should be beanFactory.setBeanClassLoader(getClassLoader()); but can't find a way to invoke inherited methods via @Alias
		beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar((ResourceLoader)(Object)this, getEnvironment()));

		// Configure the bean factory with context callbacks.
		beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor((ConfigurableApplicationContext)(Object)this));
		beanFactory.ignoreDependencyInterface(EnvironmentAware.class);
		beanFactory.ignoreDependencyInterface(EmbeddedValueResolverAware.class);
		beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);
		beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
		beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
		beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);

		// BeanFactory interface not registered as resolvable type in a plain factory.
		// MessageSource registered (and found for autowiring) as a bean.
		beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
		beanFactory.registerResolvableDependency(ResourceLoader.class, this);
		beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
		beanFactory.registerResolvableDependency(ApplicationContext.class, this);

		// Register early post-processor for detecting inner beans as ApplicationListeners.
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector((AbstractApplicationContext)(Object)this));

		// Register default environment beans.
		if (!beanFactory.containsLocalBean("environment")) {
			beanFactory.registerSingleton("environment", getEnvironment());
		}
		if (!beanFactory.containsLocalBean("systemProperties")) {
			beanFactory.registerSingleton("systemProperties", getEnvironment().getSystemProperties());
		}
		if (!beanFactory.containsLocalBean("systemEnvironment")) {
			beanFactory.registerSingleton("systemEnvironment", getEnvironment().getSystemEnvironment());
		}
	}

	@Alias
	public ConfigurableEnvironment getEnvironment() {
		return null;
	}

}
