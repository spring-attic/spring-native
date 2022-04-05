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

package org.springframework.session.server;

import org.springframework.nativex.AotOptions;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.SerializationHint;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.security.web.server.csrf.DefaultCsrfToken;
import org.springframework.session.CommonSessionSecuritySerializables;
import org.springframework.session.config.annotation.web.server.SpringWebSessionConfiguration;
import org.springframework.util.ClassUtils;


@NativeHint(trigger = SpringWebSessionConfiguration.class,
        imports = CommonSessionSecuritySerializables.class,
        serializables = @SerializationHint(types = DefaultCsrfToken.class),
        abortIfTypesMissing = true)
public class WebSessionSecurityHints implements NativeConfiguration {

    @Override
    public boolean isValid(AotOptions aotOptions) {
        // Similar to check in OnWebApplicationCondition (effectively implementing ConditionalOnWebApplication(REACTIVE))
        return ClassUtils.isPresent("org.springframework.web.server.WebSession", null)
                && ClassUtils.isPresent("org.springframework.security.web.csrf.DefaultCsrfToken", null);
    }
}
