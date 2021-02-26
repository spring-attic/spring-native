package org.springframework.nativex.substitutions.java;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.nativex.substitutions.OnlyIfPresent;

// To avoid a dependency on java.awt.Component
@TargetClass(className = "java.beans.Introspector", onlyWith = OnlyIfPresent.class)
final class Target_Introspector {

	@Substitute
	private static Class<?> findCustomizerClass(Class<?> type) {
		return null;
	}
}
