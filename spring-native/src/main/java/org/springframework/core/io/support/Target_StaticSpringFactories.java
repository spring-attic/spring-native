package org.springframework.core.io.support;

import java.util.function.Supplier;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.nativex.substitutions.OnlyIfPresent;
import org.springframework.nativex.substitutions.WithAot;
import org.springframework.util.MultiValueMap;

@TargetClass(className="org.springframework.aot.StaticSpringFactories", onlyWith = { WithAot.class, OnlyIfPresent.class })
final class Target_StaticSpringFactories {

	@Alias
	public static MultiValueMap<Class, Supplier<Object>> factories;

	@Alias
	public static MultiValueMap<Class, String> names;
}