package org.w3c.dom.bootstrap;

import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.graalvm.substitutions.OnlyPresent;
import org.springframework.graalvm.substitutions.RemoveXmlSupport;

@TargetClass(className = "org.w3c.dom.bootstrap.DOMImplementationRegistryClasses", onlyWith = { OnlyPresent.class, RemoveXmlSupport.class })
@Delete
final class Target_DOMImplementationRegistryClasses {
}