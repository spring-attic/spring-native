package org.springframework.nativex.substitutions.framework;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.context.ApplicationContext;
import org.springframework.nativex.substitutions.OnlyIfPresent;

@TargetClass(className = "org.springframework.context.support.ApplicationObjectSupport", onlyWith = OnlyIfPresent.class)
final class Target_ApplicationObjectSupport {

	@Alias
	protected ApplicationContext obtainApplicationContext() {
		return null;
	}

}
