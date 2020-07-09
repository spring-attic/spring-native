package com.wavefront.spring.actuate;

import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.OperatingSystemMXBean;
import com.sun.management.UnixOperatingSystemMXBean;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpoint;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;

@NativeImageHint(trigger = WavefrontEndpointAutoConfiguration.class, typeInfos = {
		@TypeInfo(types = { com.wavefront.java_sdk.com.google.common.util.concurrent.AbstractFuture.class,
				com.wavefront.sdk.appagent.jvm.reporter.WavefrontJvmReporter.class,
				com.wavefront.sdk.common.BufferFlusher.class,
				com.wavefront.sdk.common.WavefrontSender.class,
				com.wavefront.sdk.common.annotation.Nullable.class,
				com.wavefront.sdk.common.application.ApplicationTags.class,
				com.wavefront.sdk.common.clients.WavefrontClient.class,
				com.wavefront.sdk.entities.histograms.WavefrontHistogramSender.class,
				com.wavefront.sdk.entities.metrics.WavefrontMetricSender.class,
				com.wavefront.sdk.entities.tracing.WavefrontTracingSpanSender.class,
				com.wavefront.spring.actuate.WavefrontController.class,
				com.wavefront.spring.autoconfigure.WavefrontAutoConfiguration.class,
				com.wavefront.spring.autoconfigure.WavefrontProperties.class,
				io.micrometer.core.annotation.Timed.class,
				io.micrometer.core.instrument.Clock.class,
				io.micrometer.core.instrument.MeterRegistry.class,
				io.micrometer.core.instrument.binder.MeterBinder.class,
				io.micrometer.core.instrument.binder.jetty.JettyServerThreadPoolMetrics.class,
				io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics.class,
				io.micrometer.core.instrument.binder.jvm.JvmGcMetrics.class,
				io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics.class,
				io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics.class,
				io.micrometer.core.instrument.binder.kafka.KafkaClientMetrics.class,
				io.micrometer.core.instrument.binder.logging.Log4j2Metrics.class,
				io.micrometer.core.instrument.binder.logging.LogbackMetrics.class,
				io.micrometer.core.instrument.binder.system.FileDescriptorMetrics.class,
				io.micrometer.core.instrument.binder.system.ProcessorMetrics.class,
				io.micrometer.core.instrument.binder.system.UptimeMetrics.class,
				io.micrometer.core.instrument.binder.tomcat.TomcatMetrics.class,
				io.micrometer.core.instrument.composite.CompositeMeterRegistry.class,
				io.micrometer.core.instrument.config.MeterFilter.class,
				io.micrometer.core.instrument.config.MeterFilter.class,
				io.micrometer.core.instrument.config.MeterRegistryConfig.class,
				io.micrometer.core.instrument.push.PushMeterRegistry.class,
				io.micrometer.core.instrument.push.PushRegistryConfig.class,
				io.micrometer.core.lang.NonNullApi.class,
				io.micrometer.core.lang.NonNullFields.class,
				io.micrometer.core.lang.Nullable.class,
				ConditionalOnAvailableEndpoint.class,
				org.springframework.web.client.RestTemplate.class,
				org.springframework.http.client.SimpleClientHttpRequestFactory.class,
				ControllerEndpoint.class,
				UnixOperatingSystemMXBean.class,
				OperatingSystemMXBean.class,
				GarbageCollectionNotificationInfo.class
}, typeNames = {"com.wavefront.opentracing.reporting.Reporter",
				"com.wavefront.spring.autoconfigure.AccountManagementEnvironmentPostProcessor",
				"com.wavefront.spring.autoconfigure.WavefrontMetricsConfiguration",
				"com.wavefront.spring.autoconfigure.WavefrontTracingConfiguration",
				"com.wavefront.spring.autoconfigure.WavefrontTracingOpenTracingConfiguration",
				"com.wavefront.spring.autoconfigure.WavefrontTracingSleuthConfiguration",
				"io.micrometer.wavefront.WavefrontConfig",
				"io.micrometer.wavefront.WavefrontMeterRegistry",
				"org.springframework.boot.actuate.autoconfigure.endpoint.condition.OnAvailableEndpointCondition",
				"org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointFilter"
		})})
public class WavefrontEndpointHints implements NativeImageConfiguration {
}
