package org.springframework.context.support;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.graalvm.substitutions.FunctionalMode;
import org.springframework.graalvm.substitutions.OnlyIfPresent;
import org.springframework.graalvm.substitutions.SpringFuIsAround;

// Avoid using merged annotation infra
@TargetClass(className = "org.springframework.context.support.AbstractApplicationContext", onlyWith = { SpringFuIsAround.class, FunctionalMode.class, OnlyIfPresent.class })
final class Target_AbstractApplicationContext {

	@Substitute
	protected void resetCommonCaches() {
	}

}
