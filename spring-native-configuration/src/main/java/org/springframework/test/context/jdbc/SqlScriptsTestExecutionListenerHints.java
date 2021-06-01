package org.springframework.test.context.jdbc;

import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = SqlScriptsTestExecutionListener.class, types = @TypeHint(
		types = {
				SqlMergeMode.class, Sql.class, SqlGroup.class
		}
))
public class SqlScriptsTestExecutionListenerHints implements NativeConfiguration {
}
