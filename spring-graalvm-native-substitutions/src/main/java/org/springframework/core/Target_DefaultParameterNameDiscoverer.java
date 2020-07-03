package org.springframework.core;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.graalvm.substitutions.FunctionalMode;
import org.springframework.graalvm.substitutions.OnlyIfPresent;

// Avoid requiring org.springframework.asm.ClassReader in functional mode
@TargetClass(className = "org.springframework.core.DefaultParameterNameDiscoverer", onlyWith = { FunctionalMode.class, OnlyIfPresent.class })
final class Target_DefaultParameterNameDiscoverer {

	@Substitute
	public Target_DefaultParameterNameDiscoverer() {
	}
}
