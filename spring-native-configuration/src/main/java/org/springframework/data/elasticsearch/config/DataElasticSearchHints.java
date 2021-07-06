package org.springframework.data.elasticsearch.config;

import org.springframework.data.DataNonReactiveAuditingHints;
import org.springframework.data.elasticsearch.core.event.AuditingEntityCallback;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;


@NativeHint(
		trigger = ElasticsearchAuditingRegistrar.class,
		types = @TypeHint(types = {
				PersistentEntitiesFactoryBean.class,
				AuditingEntityCallback.class
		}),
		imports = DataNonReactiveAuditingHints.class
)
public class DataElasticSearchHints implements NativeConfiguration {

}
