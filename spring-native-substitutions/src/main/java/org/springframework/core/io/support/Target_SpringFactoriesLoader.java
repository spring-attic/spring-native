package org.springframework.core.io.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.apache.commons.logging.Log;
import org.springframework.nativex.substitutions.OnlyIfPresent;
import org.springframework.lang.Nullable;
import org.springframework.nativex.substitutions.WithBuildtools;
import org.springframework.util.Assert;

@TargetClass(className="org.springframework.core.io.support.SpringFactoriesLoader", onlyWith = { WithBuildtools.class, OnlyIfPresent.class })
final class Target_SpringFactoriesLoader {

	@Alias
	private static Log logger;

	@Substitute
	public static <T> List<T> loadFactories(Class<T> factoryType, @Nullable ClassLoader classLoader) {
		Assert.notNull(factoryType, "'factoryType' must not be null");
		List<Supplier<Object>> result = (List<Supplier<Object>>) Target_StaticSpringFactories.factories.get(factoryType);
		if (result == null) {
			return Collections.emptyList();
		}
		List<Object> list = new ArrayList<>();
		for (Supplier<Object> objectSupplier : result) {
			Object o = objectSupplier.get();
			list.add(o);
		}
		return (List<T>) list;
	}

	@Substitute
	public static List<String> loadFactoryNames(Class<?> factoryType, @Nullable ClassLoader classLoader) {
		List<String> names = Target_StaticSpringFactories.names.get(factoryType);
		if (names != null) {
			return names;
		}
		else {
			List<Supplier<Object>> stored = Target_StaticSpringFactories.factories.get(factoryType);
			if (stored == null) {
				return Collections.emptyList();
			}
			List<String> list = new ArrayList<>();
			for (Supplier<Object> objectSupplier : stored) {
				Object factory = objectSupplier.get();
				String name = factory.getClass().getName();
				list.add(name);
			}
			return list;
		}
	}

}