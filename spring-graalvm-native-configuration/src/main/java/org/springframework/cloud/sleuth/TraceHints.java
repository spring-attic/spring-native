package org.springframework.cloud.sleuth;

import org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;

@NativeImageHint(trigger = TraceAutoConfiguration.class, typeInfos = {
		@TypeInfo(types = { javax.annotation.Resource.class,
				org.aspectj.lang.annotation.Around.class,
				org.springframework.aop.aspectj.annotation.AbstractAspectJAdvisorFactory.class,
				org.aspectj.weaver.reflect.Java15AnnotationFinder.class,
				org.aspectj.weaver.reflect.Java15ReflectionBasedReferenceTypeDelegate.class,
				org.springframework.scheduling.annotation.Async.class,
				org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory.class,
				org.springframework.cloud.client.circuitbreaker.ConfigBuilder.class,
				org.springframework.web.context.request.async.WebAsyncTask.class,
				org.springframework.cloud.client.discovery.simple.SimpleDiscoveryProperties.class,
				org.springframework.cloud.client.discovery.simple.SimpleDiscoveryProperties.SimpleServiceInstance.class,
				org.springframework.cloud.sleuth.instrument.web.client.TraceWebClientAutoConfiguration.class
		}, typeNames = {
				"org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerBeanPostProcessorAutoConfiguration$OnAnyLoadBalancerImplementationPresentCondition",
				"org.springframework.cloud.context.scope.GenericScope$LockedScopedProxyFactoryBean",
				"org.springframework.cloud.sleuth.sampler.SamplerCondition$TracingCustomizerAvailable",
				"org.springframework.cloud.sleuth.sampler.SamplerCondition$SpanHandlerAvailable",
				"org.springframework.cloud.sleuth.sampler.SamplerCondition$ReporterAvailable"
		})})
public class TraceHints  implements NativeImageConfiguration  {
}
