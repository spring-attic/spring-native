package org.springframework.boot;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.core.env.Environment;
import org.springframework.graalvm.substitutions.OnlyPresent;

@TargetClass(className = "org.springframework.boot.SpringApplicationBannerPrinter", onlyWith = OnlyPresent.class)
public final class Target_SpringApplicationBannerPrinter {

	@Alias
	private static Banner DEFAULT_BANNER;

	@Substitute
	private Banner getBanner(Environment environment) {
		return DEFAULT_BANNER;
	}
}
