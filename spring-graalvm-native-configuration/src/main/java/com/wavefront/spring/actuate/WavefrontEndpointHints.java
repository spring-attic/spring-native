package com.wavefront.spring.actuate;

import com.wavefront.java_sdk.com.google.common.util.concurrent.AbstractFuture;
import com.wavefront.sdk.appagent.jvm.reporter.WavefrontJvmReporter;
import com.wavefront.sdk.common.BufferFlusher;
import com.wavefront.sdk.common.WavefrontSender;
import com.wavefront.sdk.common.annotation.Nullable;
import com.wavefront.sdk.common.application.ApplicationTags;
import com.wavefront.sdk.common.clients.WavefrontClient;
import com.wavefront.sdk.entities.histograms.WavefrontHistogramSender;
import com.wavefront.sdk.entities.metrics.WavefrontMetricSender;
import com.wavefront.sdk.entities.tracing.WavefrontTracingSpanSender;
import com.wavefront.spring.autoconfigure.WavefrontAutoConfiguration;
import com.wavefront.spring.autoconfigure.WavefrontProperties;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jetty.JettyServerThreadPoolMetrics;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.kafka.KafkaClientMetrics;
import io.micrometer.core.instrument.binder.logging.Log4j2Metrics;
import io.micrometer.core.instrument.binder.logging.LogbackMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.core.instrument.binder.tomcat.TomcatMetrics;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterRegistryConfig;
import io.micrometer.core.instrument.push.PushMeterRegistry;
import io.micrometer.core.instrument.push.PushRegistryConfig;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpoint;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@NativeImageHint(trigger = WavefrontEndpointAutoConfiguration.class, typeInfos = {
		@TypeInfo(types = {
				AbstractFuture.class,
				WavefrontJvmReporter.class,
				BufferFlusher.class,
				WavefrontSender.class,
				ApplicationTags.class,
				WavefrontClient.class,
				WavefrontHistogramSender.class,
				WavefrontMetricSender.class,
				WavefrontTracingSpanSender.class,
				WavefrontController.class,
				WavefrontAutoConfiguration.class,
				WavefrontProperties.class,
				Clock.class,
				MeterRegistry.class,
				MeterBinder.class,
				JettyServerThreadPoolMetrics.class,
				ClassLoaderMetrics.class,
				JvmGcMetrics.class,
				JvmMemoryMetrics.class,
				JvmThreadMetrics.class,
				KafkaClientMetrics.class,
				Log4j2Metrics.class,
				LogbackMetrics.class,
				FileDescriptorMetrics.class,
				ProcessorMetrics.class,
				UptimeMetrics.class,
				TomcatMetrics.class,
				CompositeMeterRegistry.class,
				MeterFilter.class,
				MeterRegistryConfig.class,
				PushMeterRegistry.class,
				PushRegistryConfig.class,
				RestTemplate.class,
				JvmGcMetrics.class,
				JvmMemoryMetrics.class,
				JvmThreadMetrics.class,
				LogbackMetrics.class,
				FileDescriptorMetrics.class,
				ProcessorMetrics.class,
				UptimeMetrics.class,
				TomcatMetrics.class,
				CompositeMeterRegistry.class,
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
		}, access = AccessBits.LOAD_AND_CONSTRUCT),
		@TypeInfo(types = {
				Timed.class,
				ConditionalOnAvailableEndpoint.class,
				ControllerEndpoint.class,
				SimpleClientHttpRequestFactory.class
		}, access = AccessBits.LOAD_AND_CONSTRUCT|AccessBits.DECLARED_METHODS)
})
public class WavefrontEndpointHints implements NativeImageConfiguration {
}
