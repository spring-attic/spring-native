package org.springframework.core.io.support;

import java.util.ArrayList;
import java.util.List;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.apache.commons.logging.Log;

import org.springframework.boot.SpringBootFactories;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.graalvm.substitutions.FunctionalMode;
import org.springframework.graalvm.substitutions.OnlyIfPresent;
import org.springframework.lang.Nullable;

@TargetClass(className="org.springframework.core.io.support.SpringFactoriesLoader", onlyWith = { FunctionalMode.class, OnlyIfPresent.class })
final class Target_SpringFactoriesLoader_Functional {

	@Alias
	private static Log logger;

	@SuppressWarnings("unchecked")
	@Substitute
	public static <T> List<T> loadFactories(Class<T> factoryType, @Nullable ClassLoader classLoader) {
		List<T> result = (List<T>) SpringBootFactories.factories.get(factoryType);
		if (result == null) {
			return new ArrayList<>();
		}
		// Error when using it, and we probably should do that at build time
		// AnnotationAwareOrderComparator.sort(result);
		return result;
	}

	@Alias
	public static List<String> loadFactoryNames(Class<?> factoryType, @Nullable ClassLoader classLoader) {
		return null;
	}

	@Alias
	private static <T> T instantiateFactory(String factoryImplementationName, Class<T> factoryType, ClassLoader classLoader) {
		return null;
	}
}
