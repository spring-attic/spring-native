package org.springframework.data.mongodb.config;

import org.springframework.data.DataNonReactiveAuditingHints;
import org.springframework.data.DataReactiveAuditingHints;
import org.springframework.data.mongodb.core.mapping.event.AuditingEntityCallback;
import org.springframework.data.mongodb.core.mapping.event.ReactiveAuditingEntityCallback;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.NativeHints;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;


@NativeHints({
		// Auditing
		@NativeHint(
				trigger = MongoAuditingRegistrar.class,
				types = @TypeHint(types = {
						PersistentEntitiesFactoryBean.class,
						AuditingEntityCallback.class,
				}),
				imports = DataNonReactiveAuditingHints.class
		),

		// Reactive Auditing
		@NativeHint(
				trigger = ReactiveMongoAuditingRegistrar.class,
				types = {@TypeHint(types = {
						PersistentEntitiesFactoryBean.class,
						ReactiveAuditingEntityCallback.class
				})},
				imports = DataReactiveAuditingHints.class
		)
})
public class MongoDbHints implements NativeConfiguration {

}
