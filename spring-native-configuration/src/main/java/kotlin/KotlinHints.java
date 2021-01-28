package kotlin;

import org.springframework.nativex.extension.NativeConfiguration;
import org.springframework.nativex.extension.NativeHint;
import org.springframework.nativex.extension.ResourcesInfo;
import org.springframework.nativex.extension.TypeInfo;

import static org.springframework.nativex.type.AccessBits.DECLARED_CONSTRUCTORS;
import static org.springframework.nativex.type.AccessBits.DECLARED_FIELDS;
import static org.springframework.nativex.type.AccessBits.DECLARED_METHODS;
import static org.springframework.nativex.type.AccessBits.PUBLIC_METHODS;

@NativeHint(
		trigger=kotlin.Unit.class,
		resourcesInfos= {
				@ResourcesInfo(patterns= {
						"META-INF/.*.kotlin_module$",
						".*.kotlin_builtins",
						"META-INF/services/.*"
				})
		}, typeInfos = {
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
