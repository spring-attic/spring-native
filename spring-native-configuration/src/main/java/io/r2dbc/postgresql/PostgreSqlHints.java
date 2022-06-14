package io.r2dbc.postgresql;

import java.net.URI;
import java.time.Instant;
import java.time.ZonedDateTime;

import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

/**
 * Hints for R2DBC PostgreSQL connector.
 *
 * @author Moritz Halbritter
 */
@NativeHint(
		trigger = PostgresqlConnectionFactoryProvider.class,
		types = {
				@TypeHint(types = { Instant[].class, ZonedDateTime[].class, URI[].class }, access = {}),
		}
)
public class PostgreSqlHints implements NativeConfiguration {
}
