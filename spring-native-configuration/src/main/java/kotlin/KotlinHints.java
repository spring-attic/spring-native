package kotlin;

import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourcesInfo;
import org.springframework.nativex.hint.TypeInfo;
import org.springframework.nativex.type.NativeConfiguration;

import static org.springframework.nativex.hint.AccessBits.*;
import static org.springframework.nativex.hint.AccessBits.DECLARED_CONSTRUCTORS;
import static org.springframework.nativex.hint.AccessBits.DECLARED_FIELDS;
import static org.springframework.nativex.hint.AccessBits.DECLARED_METHODS;
import static org.springframework.nativex.hint.AccessBits.PUBLIC_METHODS;

@NativeHint(trigger = kotlin.Unit.class,
		resources = {
				@ResourcesInfo(patterns= {
						"META-INF/.*.kotlin_module$",
						".*.kotlin_builtins",
						"META-INF/services/.*"
				})
		}, types = {
				@TypeInfo(types = kotlin.KotlinVersion.class, access = PUBLIC_METHODS | DECLARED_FIELDS | DECLARED_METHODS | DECLARED_CONSTRUCTORS),
				@TypeInfo(typeNames = {
						"kotlin.KotlinVersion[]",
						"kotlin.KotlinVersion$Companion",
						"kotlin.KotlinVersion$Companion[]"
				})
})
@NativeHint(trigger = kotlin.reflect.full.KClasses.class,
	types = {
		@TypeInfo(types = kotlin.reflect.full.KClasses.class, access = CLASS),
		@TypeInfo(types = kotlin.Metadata.class, access = DECLARED_METHODS),
		@TypeInfo(types = kotlin.reflect.jvm.internal.ReflectionFactoryImpl.class, access = DECLARED_CONSTRUCTORS),
		@TypeInfo(types = kotlin.reflect.jvm.internal.impl.resolve.scopes.DescriptorKindFilter.class, access = DECLARED_FIELDS)
	})
@NativeHint(trigger = kotlin.coroutines.Continuation.class,
		types = @TypeInfo(types = kotlin.coroutines.Continuation.class, typeNames = "kotlin.coroutines.Continuation[]", access = LOAD_AND_CONSTRUCT_AND_PUBLIC_METHODS))
@NativeHint(trigger = com.fasterxml.jackson.module.kotlin.KotlinModule.class,
		types = {
			@TypeInfo(types = com.fasterxml.jackson.module.kotlin.KotlinModule.class),
			@TypeInfo(typeNames = {
					"com.fasterxml.jackson.module.kotlin.KotlinModule$Builder",
					"com.fasterxml.jackson.module.kotlin.SingletonSupport",
					"java.lang.String"
			}, access = CLASS)
		})

public class KotlinHints implements NativeConfiguration {
}
