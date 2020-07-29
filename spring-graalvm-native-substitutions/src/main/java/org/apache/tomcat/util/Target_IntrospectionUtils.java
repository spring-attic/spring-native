package org.apache.tomcat.util;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.springframework.graalvm.substitutions.OnlyIfPresent;
import org.springframework.graalvm.substitutions.OptimizeTomcat;

@TargetClass(className = "org.apache.tomcat.util.IntrospectionUtils", onlyWith = { OnlyIfPresent.class, OptimizeTomcat.class })
public final class Target_IntrospectionUtils {

    @Substitute
    public static Object getProperty(Object o, String name) {
        return XReflectionIntrospectionUtils.getPropertyInternal(o, name);
    }

    @Substitute
    public static boolean setProperty(Object o, String name, String value, boolean invokeSetProperty) {
        return XReflectionIntrospectionUtils.setPropertyInternal(o, name, value, invokeSetProperty);
    }
}
