package org.springframework.core.annotation;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.core.Ordered;
import org.springframework.nativex.substitutions.FunctionalMode;
import org.springframework.nativex.substitutions.OnlyIfPresent;
import org.springframework.nativex.substitutions.SpringFuIsAround;

// Avoid using merged annotation infra
@TargetClass(className = "org.springframework.core.annotation.AnnotationAwareOrderComparator", onlyWith = { SpringFuIsAround.class, FunctionalMode.class, OnlyIfPresent.class })
final class Target_AnnotationAwareOrderComparator {

	@Substitute
	protected Integer findOrder(Object obj) {
		 return Ordered.LOWEST_PRECEDENCE;
	}
}
