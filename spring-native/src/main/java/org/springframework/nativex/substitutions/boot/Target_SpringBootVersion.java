package org.springframework.nativex.substitutions.boot;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.nativex.substitutions.OnlyIfPresent;

@TargetClass(className = "org.springframework.boot.SpringBootVersion", onlyWith = OnlyIfPresent.class)
final class Target_SpringBootVersion {

	@Substitute
	public static String getVersion() {
		return NativeSpringBootVersion.getVersion();
	}

}
