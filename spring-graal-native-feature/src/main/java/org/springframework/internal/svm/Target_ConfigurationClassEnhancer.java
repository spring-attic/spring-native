package org.springframework.internal.svm;

import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.TargetClass;

@Delete
@TargetClass(className = "org.springframework.context.annotation.ConfigurationClassEnhancer", onlyWith = OnlyPresent.class)
final class Target_ConfigurationClassEnhancer {
}

