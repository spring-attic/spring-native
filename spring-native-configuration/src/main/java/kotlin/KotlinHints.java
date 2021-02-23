package kotlin;

import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourcesInfo;
import org.springframework.nativex.hint.TypeInfo;

import static org.springframework.nativex.hint.AccessBits.DECLARED_CONSTRUCTORS;
import static org.springframework.nativex.hint.AccessBits.DECLARED_FIELDS;
import static org.springframework.nativex.hint.AccessBits.DECLARED_METHODS;
import static org.springframework.nativex.hint.AccessBits.PUBLIC_METHODS;

@NativeHint(
		trigger=kotlin.Unit.class,
		resources = {
				@ResourcesInfo(patterns= {
						"META-INF/.*.kotlin_module$",
						".*.kotlin_builtins",
						"META-INF/services/.*"
				})
		}, types = {
				@TypeInfo(types = kotlin.reflect.jvm.internal.ReflectionFactoryImpl.class, access = DECLARED_CONSTRUCTORS),
				@TypeInfo(types = kotlin.KotlinVersion.class, access = PUBLIC_METHODS | DECLARED_FIELDS | DECLARED_METHODS | DECLARED_CONSTRUCTORS),
				@TypeInfo(typeNames = {
						"kotlin.KotlinVersion[]",
						"kotlin.KotlinVersion$Companion",
						"kotlin.KotlinVersion$Companion[]"
				})
})
public class KotlinHints implements NativeConfiguration {
}
