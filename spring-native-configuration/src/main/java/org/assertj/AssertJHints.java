package org.assertj;

import org.assertj.core.api.Assertions;
import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.type.NativeConfiguration;

/**
 * Native hints for AssertJ.
 *
 * @author Tadaya Tsuyukubo
 */
@NativeHint(trigger = Assertions.class,
        initialization = {
                // Based on the netive-image.properties in byte buddy
                // https://github.com/raphw/byte-buddy/blob/master/byte-buddy/src/main/resources/META-INF/native-image/net.bytebuddy/byte-buddy/native-image.properties
                @InitializationHint(
                        packageNames = "org.assertj.core.internal.bytebuddy",
                        initTime = InitializationTime.BUILD)
        }
)
public class AssertJHints implements NativeConfiguration {

}
