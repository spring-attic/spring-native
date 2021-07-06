package org.springframework.data;

import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.core.DecoratingProxy;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.auditing.IsNewAwareAuditingHandler;
import org.springframework.data.domain.AuditorAware;
import org.springframework.nativex.hint.FieldHint;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.hint.TypeHints;
import org.springframework.nativex.type.NativeConfiguration;

/**
 * Hints required by all non reactive auditing auditing.
 */
@TypeHints({
		@TypeHint(types = IsNewAwareAuditingHandler.class),
		@TypeHint(types = AuditingHandler.class, fields = {
				@FieldHint(name = "auditorAware")
		})
})
@JdkProxyHint(types = {
		AuditorAware.class,
		SpringProxy.class,
		Advised.class,
		DecoratingProxy.class
})
public class DataNonReactiveAuditingHints implements NativeConfiguration {

}
