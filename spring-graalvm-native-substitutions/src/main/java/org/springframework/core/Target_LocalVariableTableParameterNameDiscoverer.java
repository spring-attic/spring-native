package org.springframework.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.graalvm.substitutions.FunctionalMode;
import org.springframework.graalvm.substitutions.OnlyIfPresent;
import org.springframework.lang.Nullable;

@TargetClass(className="org.springframework.core.LocalVariableTableParameterNameDiscoverer", onlyWith = { FunctionalMode.class, OnlyIfPresent.class })
final class Target_LocalVariableTableParameterNameDiscoverer {

	@Substitute
	public String[] getParameterNames(Method method) {
		return null;
	}

	@Substitute
	public String[] getParameterNames(Constructor<?> ctor) {
		return null;
	}
}
