package org.springframework.nativex.substitutions.boot;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.boot.WebApplicationType;
import org.springframework.nativex.substitutions.OnlyIfPresent;
import org.springframework.nativex.substitutions.WithAot;

/**
 * Why this substitution exists?
 * Because WebApplicationType#deduceFromClasspath() is package private and need to be referenced from
 * {@link Target_SpringApplication} (SpringApplication substitution).
 *
 * How this substitution workarounds the problem?
 * It provides an alias for this method.
 */
@TargetClass(className = "org.springframework.boot.WebApplicationType", onlyWith = { WithAot.class, OnlyIfPresent.class })
final class Target_WebApplicationType {

	@Alias
	static WebApplicationType deduceFromClasspath() {
		return null;
	}
}
