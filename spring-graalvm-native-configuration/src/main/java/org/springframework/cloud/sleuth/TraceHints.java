package org.springframework.cloud.sleuth;

import org.aspectj.weaver.reflect.Java15AnnotationFinder;
import org.aspectj.weaver.reflect.Java15ReflectionBasedReferenceTypeDelegate;

import org.springframework.aop.aspectj.annotation.AbstractAspectJAdvisorFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.ConfigBuilder;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryProperties;
import org.springframework.cloud.sleuth.annotation.ContinueSpan;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration;
import org.springframework.cloud.sleuth.instrument.web.client.TraceWebClientAutoConfiguration;
import org.springframework.nativex.extension.NativeImageConfiguration;
import org.springframework.nativex.extension.NativeImageHint;
import org.springframework.nativex.extension.TypeInfo;
import org.springframework.nativex.type.AccessBits;

@NativeImageHint(trigger = TraceAutoConfiguration.class, typeInfos = {
		@TypeInfo(types = {
				AbstractAspectJAdvisorFactory.class,
				Java15AnnotationFinder.class,
				Java15ReflectionBasedReferenceTypeDelegate.class,
				CircuitBreakerFactory.class,
				ConfigBuilder.class,
				SimpleDiscoveryProperties.class,
				SimpleDiscoveryProperties.SimpleServiceInstance.class,
				TraceWebClientAutoConfiguration.class,
				ContinueSpan.class,
				NewSpan.class
		}, typeNames = {
				"org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerBeanPostProcessorAutoConfiguration$OnAnyLoadBalancerImplementationPresentCondition",
				"org.springframework.cloud.context.scope.GenericScope$LockedScopedProxyFactoryBean"
		}),
		@TypeInfo(typeNames = {
				"org.springframework.cloud.sleuth.sampler.SamplerCondition",
				"org.springframework.cloud.sleuth.sampler.SamplerCondition$TracingCustomizerAvailable",
				"org.springframework.cloud.sleuth.sampler.SamplerCondition$SpanHandlerAvailable",
				"org.springframework.cloud.sleuth.sampler.SamplerCondition$ReporterAvailable",
				"org.springframework.cloud.sleuth.annotation.SleuthAdvisorConfig",
				"org.springframework.cloud.sleuth.annotation.SleuthAdvisorConfig$AnnotationClassOrMethodOrArgsPointcut",
				"org.springframework.cloud.sleuth.annotation.SleuthAdvisorConfig$AnnotationClassOrMethodFilter"
		}, access = AccessBits.ALL)
})
public class TraceHints  implements NativeImageConfiguration  {
}
