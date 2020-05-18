package org.springframework.context.annotation;

import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.graalvm.substitutions.OnlyPresent;

@Delete
@TargetClass(className = "org.springframework.context.annotation.ConfigurationClassEnhancer", onlyWith = OnlyPresent.class)
final class Target_ConfigurationClassEnhancer {
}

