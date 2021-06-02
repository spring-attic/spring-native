package org.springframework.data.jdbc.repository.config;

import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.target.AbstractBeanFactoryBasedTargetSource;
import org.springframework.aop.target.LazyInitTargetSource;
import org.springframework.core.DecoratingProxy;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.auditing.AuditingHandlerSupport;
import org.springframework.data.auditing.IsNewAwareAuditingHandler;
import org.springframework.data.domain.AuditorAware;
import org.springframework.nativex.hint.FieldHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
	trigger = JdbcAuditingRegistrar.class,
	types = {
		@TypeHint(types = IsNewAwareAuditingHandler.class),
		@TypeHint(types = LazyInitTargetSource.class),
			@TypeHint(types = AbstractBeanFactoryBasedTargetSource.class, fields = {
				@FieldHint(name = "targetBeanName"),
			}),
		@TypeHint(types = ProxyFactoryBean.class),
		@TypeHint(types = AdvisedSupport.class, fields = {
			@FieldHint(name = "targetSource"),
		}),
		@TypeHint(types = AuditingHandler.class, fields = {
			@FieldHint(name = "auditorAware"),
		}),
		@TypeHint(types = AuditingHandlerSupport.class, fields = {
			@FieldHint(name = "dateTimeForNow"),
			@FieldHint(name = "dateTimeProvider"),
			@FieldHint(name = "modifyOnCreation"),
		}),
	},
		jdkProxies = {
			@JdkProxyHint(types = {
				AuditorAware.class,
				SpringProxy.class,
				Advised.class,
				DecoratingProxy.class,
			}),
		}
)
public class DataJdbcHints implements NativeConfiguration {

}
