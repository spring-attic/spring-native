package org.springframework.context.annotation;

import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.graalvm.substitutions.OnlyIfPresent;
import org.springframework.lang.Nullable;

@TargetClass(className = "org.springframework.context.annotation.AnnotationConfigUtils", onlyWith = { FunctionalMode.class, OnlyIfPresent.class })
final class Target_AnnotationConfigUtils {

    @Substitute
    public static Set<BeanDefinitionHolder> registerAnnotationConfigProcessors(BeanDefinitionRegistry registry,
            @Nullable Object source) {
        return Collections.emptySet();
    }
}

class FunctionalMode implements Predicate<String> {

    @Override
    public boolean test(String type) {
        return System.getProperty("spring.native.mode", "default").equals("functional");
    }

}