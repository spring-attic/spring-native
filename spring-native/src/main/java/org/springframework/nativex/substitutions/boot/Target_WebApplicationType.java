package org.springframework.nativex.substitutions.boot;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.boot.WebApplicationType;
import org.springframework.nativex.substitutions.OnlyIfPresent;
import org.springframework.nativex.substitutions.WithAot;

@TargetClass(className = "org.springframework.boot.WebApplicationType", onlyWith = { WithAot.class, OnlyIfPresent.class })
final class Target_WebApplicationType {

	@Alias
	static WebApplicationType deduceFromClasspath() {
		return null;
	}
}
