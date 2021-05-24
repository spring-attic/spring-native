package org.springframework.data.jdbc.repository.config;

import org.springframework.data.auditing.AuditingHandlerSupport;
import org.springframework.data.auditing.IsNewAwareAuditingHandler;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.FieldHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
	trigger = JdbcAuditingRegistrar.class,
	types = {
		@TypeHint(types = IsNewAwareAuditingHandler.class, access = AccessBits.ALL),
		@TypeHint(types = AuditingHandlerSupport.class, fields = {
			@FieldHint(name = "dateTimeForNow"),
			@FieldHint(name = "dateTimeProvider"),
			@FieldHint(name = "modifyOnCreation"),
			@FieldHint(name = "auditorAware"),
		})
	}
)
public class DataJdbcHints implements NativeConfiguration {

}
