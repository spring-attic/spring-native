package org.springframework.data.mapping.model;


import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.graalvm.substitutions.OnlyPresent;
import org.springframework.graalvm.substitutions.RemoveCglibSupport;

@Delete
@TargetClass(className = "org.springframework.data.mapping.model.ClassGeneratingPropertyAccessorFactory", onlyWith = { OnlyPresent.class, RemoveCglibSupport.class })
final class Target_ClassGeneratingPropertyAccessorFactory {
}
