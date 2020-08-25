package org.springframework.boot.context.properties;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.graalvm.substitutions.FunctionalMode;
import org.springframework.graalvm.substitutions.OnlyIfPresent;

@TargetClass(className = "org.springframework.boot.context.properties.ConfigurationPropertiesBean", onlyWith = {
        FunctionalMode.class, OnlyIfPresent.class })
public final class Target_ConfigurationPropertiesBean {

    @Substitute
    public static <A extends Annotation> A findAnnotation(Object instance, Class<?> type, Method factory,
            Class<A> annotationType) {
        return null;
    }
}