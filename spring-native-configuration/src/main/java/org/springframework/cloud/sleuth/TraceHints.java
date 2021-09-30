package org.springframework.cloud.sleuth;

import zipkin2.reporter.AsyncReporter;

import org.springframework.cloud.sleuth.autoconfig.brave.BraveAutoConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = AsyncReporter.class, options = "--enable-url-protocols=http")
@NativeHint(trigger = BraveAutoConfiguration.class, types = {
		@TypeHint(typeNames = {"org.springframework.cloud.context.scope.GenericScope$LockedScopedProxyFactoryBean"})
})
public class TraceHints implements NativeConfiguration {
}