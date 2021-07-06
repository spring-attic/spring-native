package org.springframework.data.r2dbc.config;

import org.springframework.data.DataReactiveAuditingHints;
import org.springframework.data.r2dbc.mapping.event.ReactiveAuditingEntityCallback;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
		trigger = R2dbcAuditingRegistrar.class,
		types = {
				@TypeHint(types = PersistentEntitiesFactoryBean.class),
				@TypeHint(types = ReactiveAuditingEntityCallback.class),
		},
		imports = DataReactiveAuditingHints.class
)
public class DataR2dbcHints implements NativeConfiguration {

}
