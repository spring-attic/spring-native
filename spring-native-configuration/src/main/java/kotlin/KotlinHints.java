package kotlin;

import org.springframework.nativex.extension.NativeImageConfiguration;
import org.springframework.nativex.extension.NativeImageHint;
import org.springframework.nativex.extension.ResourcesInfo;

@NativeImageHint(
		trigger=kotlin.Unit.class,
		resourcesInfos= {
				@ResourcesInfo(patterns= {
						"META-INF/.*.kotlin_module$",
						".*.kotlin_builtins",
						"META-INF/services/.*"
				})
		}
)
public class KotlinHints implements NativeImageConfiguration {
}
