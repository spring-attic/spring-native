package org.apache.catalina.authenticator.jaspic;

import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.graalvm.substitutions.OnlyIfPresent;

@Delete
@TargetClass(className = "org.apache.catalina.authenticator.jaspic.AuthConfigFactoryImpl", onlyWith = { OnlyIfPresent.class })
final class Target_AuthConfigFactoryImpl {
}
