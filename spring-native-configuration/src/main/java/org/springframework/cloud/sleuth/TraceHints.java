package org.springframework.cloud.sleuth;

import zipkin2.reporter.AsyncReporter;

import org.springframework.cloud.sleuth.autoconfig.brave.BraveAutoConfiguration;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = AsyncReporter.class, options = "--enable-url-protocols=http")
@NativeHint(trigger = BraveAutoConfiguration.class, types = {
		@TypeHint(typeNames = {"org.springframework.cloud.context.scope.GenericScope$LockedScopedProxyFactoryBean", "brave.kafka.clients.TracingProducer", "brave.kafka.clients.TracingConsumer"})
}, jdkProxies = {
		@JdkProxyHint(typeNames = {
				"org.springframework.transaction.support.ResourceTransactionManager",
				"org.springframework.beans.factory.BeanFactoryAware",
				"org.springframework.beans.factory.InitializingBean",
				"org.springframework.transaction.PlatformTransactionManager",
				"java.io.Serializable",
				"org.springframework.aop.SpringProxy",
				"org.springframework.aop.framework.Advised",
				"org.springframework.core.DecoratingProxy"}),
		@JdkProxyHint(typeNames = {
				"org.springframework.transaction.support.ResourceTransactionManager",
				"org.springframework.beans.factory.BeanFactoryAware",
				"org.springframework.beans.factory.InitializingBean",
				"org.springframework.transaction.ReactiveTransactionManager",
				"java.io.Serializable",
				"org.springframework.aop.SpringProxy",
				"org.springframework.aop.framework.Advised",
				"org.springframework.core.DecoratingProxy"})
})
public class TraceHints implements NativeConfiguration {
}
