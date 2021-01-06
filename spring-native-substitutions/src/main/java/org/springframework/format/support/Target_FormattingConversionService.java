package org.springframework.format.support;

import java.lang.annotation.Annotation;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.nativex.substitutions.FunctionalMode;
import org.springframework.nativex.substitutions.OnlyIfPresent;
import org.springframework.nativex.substitutions.SpringFuIsAround;

// Avoid using merged annotation infra
@TargetClass(className = "org.springframework.format.support.FormattingConversionService", onlyWith = { SpringFuIsAround.class, FunctionalMode.class, OnlyIfPresent.class })
final class Target_FormattingConversionService {

	@Substitute
	public void addFormatterForFieldAnnotation(AnnotationFormatterFactory<? extends Annotation> annotationFormatterFactory) {
	}
}
