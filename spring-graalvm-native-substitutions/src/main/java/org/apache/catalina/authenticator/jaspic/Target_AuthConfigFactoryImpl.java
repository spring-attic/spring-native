package org.apache.catalina.authenticator.jaspic;

import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.graalvm.substitutions.OnlyPresent;

@Delete
@TargetClass(className = "org.apache.catalina.authenticator.jaspic.AuthConfigFactoryImpl", onlyWith = { OnlyPresent.class })
final class Target_AuthConfigFactoryImpl {
}
