package org.springframework.cloud.sleuth;

import org.aspectj.weaver.reflect.Java15AnnotationFinder;
import org.aspectj.weaver.reflect.Java15ReflectionBasedReferenceTypeDelegate;

import org.springframework.aop.aspectj.annotation.AbstractAspectJAdvisorFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.ConfigBuilder;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryProperties;
import org.springframework.cloud.sleuth.annotation.ContinueSpan;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.autoconfig.brave.BraveAutoConfiguration;
import org.springframework.cloud.sleuth.autoconfig.instrument.quartz.TraceQuartzAutoConfiguration;
import org.springframework.cloud.sleuth.autoconfig.instrument.web.TraceWebAutoConfiguration;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = BraveAutoConfiguration.class, types = {
		@TypeHint(types = {
				AbstractAspectJAdvisorFactory.class,
				Java15AnnotationFinder.class,
				Java15ReflectionBasedReferenceTypeDelegate.class,
				CircuitBreakerFactory.class,
				ConfigBuilder.class,
				SimpleDiscoveryProperties.class,
				TraceWebAutoConfiguration.class,
				ContinueSpan.class,
				NewSpan.class
		}, typeNames = {
				"org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerBeanPostProcessorAutoConfiguration$OnAnyLoadBalancerImplementationPresentCondition",
				"org.springframework.cloud.context.scope.GenericScope$LockedScopedProxyFactoryBean"
		}),
		@TypeHint(types = TraceQuartzAutoConfiguration.class,
				typeNames = {
						"org.springframework.cloud.sleuth.autoconfig.brave.SamplerCondition",
						"org.springframework.cloud.sleuth.autoconfig.brave.SamplerCondition$TracingCustomizerAvailable",
						"org.springframework.cloud.sleuth.autoconfig.brave.SamplerCondition$SpanHandlerAvailable",
						"org.springframework.cloud.sleuth.autoconfig.brave.SamplerCondition$ReporterAvailable",
						"org.springframework.cloud.sleuth.autoconfig.brave.SamplerCondition$SpanHandlerOtherThanCompositePresent",
						"org.springframework.cloud.sleuth.instrument.annotation.SleuthAdvisorConfig",
						"org.springframework.cloud.sleuth.instrument.annotation.SleuthAdvisorConfig$AnnotationClassOrMethodOrArgsPointcut",
						"org.springframework.cloud.sleuth.instrument.annotation.SleuthAdvisorConfig$AnnotationClassOrMethodFilter",
						"org.springframework.cloud.sleuth.autoconfig.brave.AnyTracerModePropertySetCondition$OnConcreteTracerMode",
						"org.springframework.cloud.sleuth.autoconfig.brave.AnyTracerModePropertySetCondition$OnAutoTracerMode",
						"org.springframework.cloud.sleuth.autoconfig.zipkin2.ZipkinRestTemplateSenderConfiguration",
						"org.springframework.cloud.sleuth.autoconfig.zipkin2.ZipkinRestTemplateSenderConfiguration$DefaultZipkinUrlExtractorConfiguration",
						"org.springframework.cloud.sleuth.autoconfig.zipkin2.ZipkinRestTemplateSenderConfiguration$DiscoveryClientZipkinUrlExtractorConfiguration",
						"org.springframework.cloud.sleuth.autoconfig.zipkin2.ZipkinRestTemplateSenderConfiguration$DiscoveryClientZipkinUrlExtractorConfiguration$ZipkinClientLoadBalancedConfiguration",
						"org.springframework.cloud.sleuth.autoconfig.zipkin2.ZipkinRestTemplateSenderConfiguration$DiscoveryClientZipkinUrlExtractorConfiguration$ZipkinClientNoOpConfiguration"
				}, access = AccessBits.ALL)
})
public class TraceHints implements NativeConfiguration {
}