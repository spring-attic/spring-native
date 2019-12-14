package org.springframework.boot;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.core.env.Environment;

@TargetClass(className = "org.springframework.boot.SpringApplicationBannerPrinter")
public final class Target_SpringApplicationBannerPrinter {

	@Substitute
	private Banner getBanner(Environment environment) {
		return new SpringBootBanner();
	}
}
