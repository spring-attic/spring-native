package org.springframework.data.jdbc.repository.config;

import org.springframework.context.annotation.Import;
import org.springframework.data.DataAuditingHints;
import org.springframework.data.DataNonReactiveAuditingHints;
import org.springframework.data.relational.core.mapping.event.RelationalAuditingCallback;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@Import(DataAuditingHints.class)
@NativeHint(
		trigger = JdbcAuditingRegistrar.class,
		types = @TypeHint(types = RelationalAuditingCallback.class),
		imports = DataNonReactiveAuditingHints.class
)
public class DataJdbcHints implements NativeConfiguration {
}