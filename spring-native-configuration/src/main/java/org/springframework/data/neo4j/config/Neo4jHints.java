package org.springframework.data.neo4j.config;

import org.springframework.data.DataNonReactiveAuditingHints;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.mongodb.core.mapping.event.AuditingEntityCallback;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;


@NativeHint(
		trigger = Neo4jAuditingRegistrar.class,
		types = @TypeHint(types = {
				PersistentEntities.class,
				AuditingEntityCallback.class,
		}),
		imports = DataNonReactiveAuditingHints.class
)
public class Neo4jHints implements NativeConfiguration {

}
