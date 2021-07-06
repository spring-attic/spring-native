package org.springframework.data.jpa.repository.config;

import org.springframework.beans.factory.aspectj.AnnotationBeanConfigurerAspect;
import org.springframework.context.annotation.Import;
import org.springframework.data.DataAuditingHints;
import org.springframework.data.DataNonReactiveAuditingHints;
import org.springframework.data.jpa.domain.support.AuditingBeanFactoryPostProcessor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@Import(DataAuditingHints.class)
@NativeHint(
		trigger = JpaAuditingRegistrar.class,
		types = @TypeHint(types = {
				AnnotationBeanConfigurerAspect.class,
				AuditingBeanFactoryPostProcessor.class,
				AuditingEntityListener.class
		}),
		imports = DataNonReactiveAuditingHints.class
)
public class DataJpaHints implements NativeConfiguration {

}
