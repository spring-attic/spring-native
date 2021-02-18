package org.springframework.core.io.support;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.nativex.substitutions.OnlyIfPresent;
import org.springframework.nativex.substitutions.WithAot;

@TargetClass(className="org.springframework.boot.SpringApplication", onlyWith = { WithAot.class, OnlyIfPresent.class })
final class Target_SpringApplication {

	@Substitute
	private <T> Collection<T> getSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes, Object... args) {
		ClassLoader classLoader = getClassLoader();
		// Use names and ensure unique to protect against duplicates
		Set<String> names = new LinkedHashSet<>(SpringFactoriesLoader.loadFactoryNames(type, classLoader));
		List<T> instances;
		if (args.length == 0) {
			instances = SpringFactoriesLoader.loadFactories(type, classLoader);
		} else {
			// TODO generate reflection data when args are passed
			instances = createSpringFactoriesInstances(type, parameterTypes, classLoader, args, names);
		}
		AnnotationAwareOrderComparator.sort(instances);
		return instances;
	}

	@Alias
	public ClassLoader getClassLoader() {
		return null;
	}

	@Alias
	private <T> List<T> createSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes,
			ClassLoader classLoader, Object[] args, Set<String> names) {
		return null;
	}
}