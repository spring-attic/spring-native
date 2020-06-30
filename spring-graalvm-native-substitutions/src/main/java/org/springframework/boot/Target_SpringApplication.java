package org.springframework.boot;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.boot.context.event.EventPublishingRunListener;
import org.springframework.boot.diagnostics.DiagnosticsProvider;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.graalvm.substitutions.OnlyIfPresent;

@TargetClass(className = "org.springframework.boot.SpringApplication", onlyWith = { OnlyIfPresent.class })
final class Target_SpringApplication {

	@SuppressWarnings("unchecked")
	@Substitute
	private <T> Collection<T> getSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes, Object... args) {
		List<T> instances;
		if (type.equals(SpringApplicationRunListener.class)) {
			instances = (List<T>) Arrays.asList(new EventPublishingRunListener((SpringApplication)(Object)this, new String[0])); // TODO convert args
			// Error when using it, and we probably should do that at build time
			//AnnotationAwareOrderComparator.sort(instances);
		}
		else if (type.equals(SpringBootExceptionReporter.class)) {
			instances = (List<T>) Arrays.asList(DiagnosticsProvider.getFailureAnalyzers((ConfigurableApplicationContext) args[0])); // Package private
			// Error when using it, and we probably should do that at build time
			//AnnotationAwareOrderComparator.sort(instances);
		}
		else {
			instances = SpringFactoriesLoader.loadFactories(type, null);
		}
		return instances;
	}
}
