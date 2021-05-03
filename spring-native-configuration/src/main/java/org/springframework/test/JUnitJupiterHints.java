package org.springframework.test;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ProxyHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = org.junit.jupiter.api.Test.class, types = {
		@TypeHint(types = {
				org.springframework.test.context.junit.jupiter.SpringExtension.class,
				org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.class,
				org.springframework.test.context.support.DefaultBootstrapContext.class,
				org.springframework.boot.test.context.SpringBootTestContextBootstrapper.class,
				org.springframework.boot.test.context.SpringBootContextLoader.class,
				org.springframework.boot.test.autoconfigure.SpringBootDependencyInjectionTestExecutionListener.class,
				org.springframework.boot.test.mock.mockito.MockitoPostProcessor.class
		}, typeNames = "org.springframework.boot.test.mock.mockito.MockitoPostProcessor$SpyPostProcessor"),
		@TypeHint(types = {
				org.springframework.boot.test.context.SpringBootTest.class,
				org.springframework.test.context.web.WebAppConfiguration.class,
				org.springframework.test.context.BootstrapWith.class,
				SpringBootConfiguration.class
		}, access = AccessBits.ANNOTATION)
}, proxies = {
		@ProxyHint(types = { org.springframework.test.context.BootstrapWith.class, org.springframework.core.annotation.SynthesizedAnnotation.class }),
		@ProxyHint(types = { org.springframework.boot.test.context.SpringBootTest.class, org.springframework.core.annotation.SynthesizedAnnotation.class }),
})
public class JUnitJupiterHints implements NativeConfiguration {
}
