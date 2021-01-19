package org.springframework.core.io.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.springframework.lang.Nullable;
import org.springframework.nativex.substitutions.WithBuildtools;
import org.springframework.util.Assert;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

@TargetClass(className="org.springframework.core.io.support.SpringFactoriesLoader", onlyWith = { WithBuildtools.class, OnlyIfPresent.class })
final class Target_SpringFactoriesLoader {

	@Alias
	private static Log logger;

	@SuppressWarnings("unchecked")
	@Substitute
	public static <T> List<T> loadFactories(Class<T> factoryType, @Nullable ClassLoader classLoader) {
		Assert.notNull(factoryType, "'factoryType' must not be null");
		List<Supplier<Object>> result = (List<Supplier<Object>>) Target_StaticSpringFactories.factories.get(factoryType);
		if (result == null) {
			return Collections.emptyList();
		}
		List<T> facs = new ArrayList<>();
		for (Supplier<Object> supplier: result) {
			facs.add((T) supplier.get());
		}
		return facs;
//		return (List<T>) result.stream().map(Supplier::get).collect(Collectors.toList());
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
			List<String> result = new ArrayList<>();
			for (Supplier<Object> supplier: stored) {
				result.add(supplier.get().getClass().getName());
			}
			return result;
//			return stored.stream().map(Supplier::get).map(factory -> factory.getClass().getName()).collect(Collectors.toList());
		}
	}

}