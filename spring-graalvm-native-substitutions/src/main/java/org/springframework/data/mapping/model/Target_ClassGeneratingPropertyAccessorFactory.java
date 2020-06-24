package org.springframework.data.mapping.model;


import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.graalvm.substitutions.OnlyIfPresent;

@Delete
@TargetClass(className = "org.springframework.data.mapping.model.ClassGeneratingPropertyAccessorFactory", onlyWith = { OnlyIfPresent.class })
final class Target_ClassGeneratingPropertyAccessorFactory {
}
