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

package org.springframework.nativex.substitutions.boot;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.util.Instantiator;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.nativex.substitutions.OnlyIfPresent;
import org.springframework.nativex.substitutions.WithAot;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

/**
 * Why this substitution exists?
 * To avoid using SpringFactoriesLoader#loadFactoryNames() in order to use reflection-less variant when possible.
 *
 * How this substitution workarounds the problem?
 * It invokes SpringFactoriesLoader#loadFactories() when possible instead (which is using underneath StaticSpringFactories generated AOT).
 */
@TargetClass(className="org.springframework.boot.sql.init.dependency.DatabaseInitializationDependencyConfigurer$DependsOnDatabaseInitializationPostProcessor", onlyWith = { WithAot.class, OnlyIfPresent.class })
final class Target_DatabaseInitializationDependencyConfigurer {

	@Alias
	private Environment environment;

	@Substitute
	private <T> List<T> getDetectors(ConfigurableListableBeanFactory beanFactory, Class<T> type) {
		List<String> factoryNames = SpringFactoriesLoader.loadFactoryNames(type, beanFactory.getBeanClassLoader());
		List<T> factories = SpringFactoriesLoader.loadFactories(type, beanFactory.getBeanClassLoader());
		
		// factories may be shorter than factoryNames if some of the named types have non simple constructors
		List<String> missing = new ArrayList<>(factoryNames);
		for (T f: factories) {
			missing.remove(f.getClass().getName());
		}

		// Now attempt to reflectively define those missing ones (the reflection entries should have been added by
		// the StaticSpringFactories builder code in Aot)
		
		// Instantiation code as per the substituted location
		Instantiator<T> instantiator = new EnvironmentAwareInstantiator<>(type, this.environment);
		List<T> instantiatedMissingFactories = instantiator.instantiate(beanFactory.getBeanClassLoader(), missing);
		List<T> result = new ArrayList<>();
		result.addAll(factories);
		result.addAll(instantiatedMissingFactories);
		AnnotationAwareOrderComparator.sort(result);
		return result;
	}
	
	public static class EnvironmentAwareInstantiator<T> extends Instantiator<T> {
		EnvironmentAwareInstantiator(Class<T> type, Environment environment) {
			super(type, (availableParameters) -> availableParameters.add(Environment.class, environment));
		}
	}

}
