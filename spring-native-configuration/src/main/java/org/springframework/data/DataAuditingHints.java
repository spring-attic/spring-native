package org.springframework.data;

import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.target.AbstractBeanFactoryBasedTargetSource;
import org.springframework.aop.target.LazyInitTargetSource;
import org.springframework.beans.factory.config.ObjectFactoryCreatingFactoryBean;
import org.springframework.data.auditing.AuditingHandlerSupport;
import org.springframework.data.auditing.config.AuditingBeanDefinitionRegistrarSupport;
import org.springframework.nativex.hint.FieldHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

/**
 * Hints required for all auditing.
 */
@NativeHint(
		trigger = AuditingBeanDefinitionRegistrarSupport.class,
		types = {
				@TypeHint(types = {LazyInitTargetSource.class,
						ObjectFactoryCreatingFactoryBean.class,
						ProxyFactoryBean.class}),
				@TypeHint(types = AbstractBeanFactoryBasedTargetSource.class, fields = {
						@FieldHint(name = "targetBeanName")}),
				@TypeHint(types = AdvisedSupport.class, fields = {
						@FieldHint(name = "targetSource")}),
				@TypeHint(types = AuditingHandlerSupport.class, fields = {
						@FieldHint(name = "dateTimeForNow"),
						@FieldHint(name = "dateTimeProvider"),
						@FieldHint(name = "modifyOnCreation")})
		})
public class DataAuditingHints implements NativeConfiguration {

}
