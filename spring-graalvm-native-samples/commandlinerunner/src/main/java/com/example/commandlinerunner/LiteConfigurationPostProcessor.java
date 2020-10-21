package com.example.commandlinerunner;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.util.ClassUtils;

public class LiteConfigurationPostProcessor implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    @Override
    @SuppressWarnings("serial")
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        for (String name : registry.getBeanDefinitionNames()) {
            BeanDefinition bean = registry.getBeanDefinition(name);
            if (bean instanceof AnnotatedGenericBeanDefinition) {
                String typeName = bean.getBeanClassName();
                AnnotatedGenericBeanDefinition anno = ((AnnotatedGenericBeanDefinition)bean);
                if (typeName!=null && ClassUtils.isPresent(typeName.replace("$", "_") + "Cached", null)) {
                    bean = new AnnotatedGenericBeanDefinition(anno.getMetadata()) {
                        @Override
                        public void setAttribute(String name, Object value) {
                            if (name.equals("org.springframework.context.annotation.ConfigurationClassUtils.configurationClass")) {
                                super.setAttribute(name, "lite");
                                return;
                            }
                            super.setAttribute(name, value);
                        }
                    };
                    bean.setBeanClassName(typeName.replace("$", "_") + "Cached");
                    registry.removeBeanDefinition(name);
                    registry.registerBeanDefinition(name, bean);
                }
            }
        }
    }
    
}
