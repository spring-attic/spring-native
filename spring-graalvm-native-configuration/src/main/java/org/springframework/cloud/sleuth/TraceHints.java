package org.springframework.cloud.sleuth;

import javax.annotation.Resource;

import org.aspectj.lang.annotation.Around;
import org.aspectj.weaver.reflect.Java15AnnotationFinder;
import org.aspectj.weaver.reflect.Java15ReflectionBasedReferenceTypeDelegate;

import org.springframework.aop.aspectj.annotation.AbstractAspectJAdvisorFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.ConfigBuilder;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryProperties;
import org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration;
import org.springframework.cloud.sleuth.instrument.web.client.TraceWebClientAutoConfiguration;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;

@NativeImageHint(trigger = TraceAutoConfiguration.class, typeInfos = {
		@TypeInfo(types = { Resource.class,
				Around.class,
				AbstractAspectJAdvisorFactory.class,
				Java15AnnotationFinder.class,
				Java15ReflectionBasedReferenceTypeDelegate.class,
				CircuitBreakerFactory.class,
				ConfigBuilder.class,
				SimpleDiscoveryProperties.class,
				SimpleDiscoveryProperties.SimpleServiceInstance.class,
				TraceWebClientAutoConfiguration.class
		}, typeNames = {
				"org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerBeanPostProcessorAutoConfiguration$OnAnyLoadBalancerImplementationPresentCondition",
				"org.springframework.cloud.context.scope.GenericScope$LockedScopedProxyFactoryBean"
		}, access = AccessBits.LOAD_AND_CONSTRUCT),
		@TypeInfo(typeNames = {
				"org.springframework.cloud.sleuth.sampler.SamplerCondition$TracingCustomizerAvailable",
				"org.springframework.cloud.sleuth.sampler.SamplerCondition$SpanHandlerAvailable",
				"org.springframework.cloud.sleuth.sampler.SamplerCondition$ReporterAvailable"
		})

})
public class TraceHints  implements NativeImageConfiguration  {
}
