package org.springframework.nativex.substitutions.boot;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;
import org.springframework.nativex.substitutions.OnlyIfPresent;

@TargetClass(className = "org.springframework.boot.SpringApplicationBannerPrinter", onlyWith = OnlyIfPresent.class)
final class Target_SpringApplicationBannerPrinter {

	@Alias
	private static Banner DEFAULT_BANNER;

	@Substitute
	private Banner getBanner(Environment environment) {
		return DEFAULT_BANNER;
	}
}
