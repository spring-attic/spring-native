package org.apache.catalina.core;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.nativex.substitutions.OnlyIfPresent;

@TargetClass(className = "org.apache.catalina.core.AprLifecycleListener", onlyWith = { OnlyIfPresent.class })
final class Target_AprLifecycleListener {

	@Substitute
	public static boolean isAprAvailable() {
		return false;
	}
}
