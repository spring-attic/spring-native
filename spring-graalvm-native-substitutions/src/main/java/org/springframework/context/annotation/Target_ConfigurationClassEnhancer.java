package org.springframework.context.annotation;

import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.graalvm.substitutions.OnlyPresent;
import org.springframework.graalvm.substitutions.RemoveCglibSupport;

@Delete
@TargetClass(className = "org.springframework.context.annotation.ConfigurationClassEnhancer", onlyWith = { OnlyPresent.class, RemoveCglibSupport.class })
final class Target_ConfigurationClassEnhancer {
}

