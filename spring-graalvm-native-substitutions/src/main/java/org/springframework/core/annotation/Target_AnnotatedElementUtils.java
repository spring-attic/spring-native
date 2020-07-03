package org.springframework.core.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.graalvm.substitutions.FunctionalMode;
import org.springframework.graalvm.substitutions.OnlyIfPresent;
import org.springframework.graalvm.substitutions.SpringFuIsAround;

// Avoid using merged annotation infra
@TargetClass(className = "org.springframework.core.annotation.AnnotatedElementUtils", onlyWith = { SpringFuIsAround.class, FunctionalMode.class, OnlyIfPresent.class })
final class Target_AnnotatedElementUtils {

	@Substitute
	public static <A extends Annotation> A getMergedAnnotation(AnnotatedElement element, Class<A> annotationType) {
			return element.getDeclaredAnnotation(annotationType);

	}
}
