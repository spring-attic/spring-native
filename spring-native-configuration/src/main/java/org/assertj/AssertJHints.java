/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
                // Based on the native-image.properties in byte buddy
                // https://github.com/raphw/byte-buddy/blob/master/byte-buddy/src/main/resources/META-INF/native-image/net.bytebuddy/byte-buddy/native-image.properties
                @InitializationHint(
                        packageNames = "org.assertj.core.internal.bytebuddy",
                        initTime = InitializationTime.BUILD)
        }
)
public class AssertJHints implements NativeConfiguration {

}
