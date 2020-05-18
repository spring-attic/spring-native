package org.springframework.context.event;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.graalvm.substitutions.OnlyPresent;
import org.springframework.graalvm.substitutions.RemoveSpelSupport;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

@TargetClass(className = "org.springframework.context.event.EventListenerMethodProcessor", onlyWith = { OnlyPresent.class, RemoveSpelSupport.class })
final class Target_EventListenerMethodProcessor {

	@Alias
	protected Log logger;

	@Alias
	private Set<Class<?>> nonAnnotatedClasses;

	@Alias
	private ConfigurableApplicationContext applicationContext;

	@Alias
	private List<EventListenerFactory> eventListenerFactories;

	@Delete
	private EventExpressionEvaluator evaluator;

	@Substitute
	public Target_EventListenerMethodProcessor() {
		this.logger = LogFactory.getLog(getClass());
		this.nonAnnotatedClasses = nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));
	}

	@Substitute
	private void processBean(final String beanName, final Class<?> targetType) {
		if (!this.nonAnnotatedClasses.contains(targetType) &&
				AnnotationUtils.isCandidateClass(targetType, EventListener.class) &&
				!isSpringContainerClass(targetType)) {

			Map<Method, EventListener> annotatedMethods = null;
			try {
				annotatedMethods = MethodIntrospector.selectMethods(targetType,
						new MethodIntrospector.MetadataLookup<EventListener>() {
							@Override
							public EventListener inspect(Method method) {
								return AnnotatedElementUtils.findMergedAnnotation(method, EventListener.class);
							}
						});
			}
			catch (Throwable ex) {
				// An unresolvable type in a method signature, probably from a lazy bean - let's ignore it.
				if (logger.isDebugEnabled()) {
					logger.debug("Could not resolve methods for bean with name '" + beanName + "'", ex);
				}
			}

			if (CollectionUtils.isEmpty(annotatedMethods)) {
				this.nonAnnotatedClasses.add(targetType);
				if (logger.isTraceEnabled()) {
					logger.trace("No @EventListener annotations found on bean class: " + targetType.getName());
				}
			}
			else {
				// Non-empty set of methods
				ConfigurableApplicationContext context = this.applicationContext;
				Assert.state(context != null, "No ApplicationContext set");
				List<EventListenerFactory> factories = this.eventListenerFactories;
				Assert.state(factories != null, "EventListenerFactory List not initialized");
				for (Method method : annotatedMethods.keySet()) {
					for (EventListenerFactory factory : factories) {
						if (factory.supportsMethod(method)) {
							Method methodToUse = AopUtils.selectInvocableMethod(method, context.getType(beanName));
							ApplicationListener<?> applicationListener =
									factory.createApplicationListener(beanName, targetType, methodToUse);
							context.addApplicationListener(applicationListener);
							break;
						}
					}
				}
				if (logger.isDebugEnabled()) {
					logger.debug(annotatedMethods.size() + " @EventListener methods processed on bean '" +
							beanName + "': " + annotatedMethods);
				}
			}
		}
	}

	@Alias
	private static boolean isSpringContainerClass(Class<?> clazz) {
		return false;
	}
}
