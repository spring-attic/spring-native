package org.springframework.nativex.substitutions.boot;


import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.nativex.substitutions.OnlyIfPresent;
import org.springframework.nativex.substitutions.WithAot;

/**
 * Why this substitution exists?
 * Because BeanDefinitionLoader is package private and need to be referenced from {@link Target_SpringApplication} (SpringApplication substitution).
 *
 * How this substitution workarounds the problem?
 * It provides aliases for some its methods.
 */
@TargetClass(className = "org.springframework.boot.BeanDefinitionLoader", onlyWith = { WithAot.class, OnlyIfPresent.class })
final class Target_BeanDefinitionLoader {

	@Alias
	void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
	}

	@Alias
	void setResourceLoader(ResourceLoader resourceLoader) {
	}

	@Alias
	void setEnvironment(ConfigurableEnvironment environment) {
	}

	@Alias
	void load() {
	}
}
