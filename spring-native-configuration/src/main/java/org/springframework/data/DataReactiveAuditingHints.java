package org.springframework.data;

import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.core.DecoratingProxy;
import org.springframework.data.auditing.ReactiveAuditingHandler;
import org.springframework.data.auditing.ReactiveIsNewAwareAuditingHandler;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.nativex.hint.FieldHint;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.hint.TypeHints;
import org.springframework.nativex.type.NativeConfiguration;

/**
 * Hints required by all reactive auditing.
 */
@TypeHints({
		@TypeHint(types = ReactiveIsNewAwareAuditingHandler.class),
		@TypeHint(types = ReactiveAuditingHandler.class, fields = {
				@FieldHint(name = "auditorAware")
		})
})
@JdkProxyHint(types = {
		ReactiveAuditorAware.class,
		SpringProxy.class,
		Advised.class,
		DecoratingProxy.class
})

public class DataReactiveAuditingHints implements NativeConfiguration {

}
