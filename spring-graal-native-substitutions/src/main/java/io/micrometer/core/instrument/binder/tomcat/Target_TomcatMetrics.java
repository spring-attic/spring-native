package io.micrometer.core.instrument.binder.tomcat;

import org.springframework.internal.svm.OnlyPresent;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * Attempting to prevent TomcatMetrics getting into JMX tinkering.
 * 
 * @author Andy Clement
 */
@TargetClass(className = "io.micrometer.core.instrument.binder.tomcat.TomcatMetrics", onlyWith = OnlyPresent.class)
public final class Target_TomcatMetrics {

	@Substitute
    public void bindTo(MeterRegistry registry) {}
}
