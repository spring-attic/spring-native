package org.springframework.core.io.support;

import java.util.ArrayList;
import java.util.List;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.apache.commons.logging.Log;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.graalvm.substitutions.OnlyPresent;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

@TargetClass(className="org.springframework.core.io.support.SpringFactoriesLoader", onlyWith = { OnlyPresent.class })
final class Target_SpringFactoriesLoader {

	@Alias
	private static Log logger;

	// Workaround in order to wait for a fix for https://github.com/oracle/graal/issues/2490
	@Substitute
	public static <T> List<T> loadFactories(Class<T> factoryType, @Nullable ClassLoader classLoader) {
		Assert.notNull(factoryType, "'factoryType' must not be null");
		ClassLoader classLoaderToUse = classLoader;
		if (classLoaderToUse == null) {
			classLoaderToUse = SpringFactoriesLoader.class.getClassLoader();
		}
		List<String> factoryImplementationNames = loadFactoryNames(factoryType, classLoaderToUse);
		if (logger.isTraceEnabled()) {
			logger.trace("Loaded [" + factoryType.getName() + "] names: " + factoryImplementationNames);
		}
		List<T> result = new ArrayList<>(factoryImplementationNames.size());
		for (String factoryImplementationName : factoryImplementationNames) {
			if (!factoryImplementationName.startsWith("org.springframework.boot.test") && !factoryImplementationName.startsWith("org.springframework.test")) {
				result.add(instantiateFactory(factoryImplementationName, factoryType, classLoaderToUse));
			}
		}
		AnnotationAwareOrderComparator.sort(result);
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
