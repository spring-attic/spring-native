package org.springframework.boot.autoconfigure.cache;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.core.type.AnnotationMetadata;
import org.springframework.nativex.substitutions.OnlyIfPresent;

@TargetClass(className = "org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration", innerClass = "CacheConfigurationImportSelector", onlyWith = OnlyIfPresent.class)
final class Target_CacheConfigurationImportSelector {

	// TODO Import dynamically the required caching configuration as described in https://github.com/spring-projects-experimental/spring-native/issues/465
	@Substitute
	public String[] selectImports(AnnotationMetadata importingClassMetadata) {
		return new String[0];
	}
}
