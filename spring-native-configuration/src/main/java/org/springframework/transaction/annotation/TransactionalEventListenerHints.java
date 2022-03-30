package org.springframework.transaction.annotation;

import org.springframework.core.annotation.SynthesizedAnnotation;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * @author Moritz Halbritter
 */
@NativeHint(trigger = TransactionalEventListener.class, jdkProxies = {
		@JdkProxyHint(types = {
				TransactionalEventListener.class,
				SynthesizedAnnotation.class
		})
})
public class TransactionalEventListenerHints implements NativeConfiguration {
}
